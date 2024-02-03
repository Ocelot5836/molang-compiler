package gg.moonflower.molangcompiler.core.compiler;

import gg.moonflower.molangcompiler.api.exception.MolangSyntaxException;
import gg.moonflower.molangcompiler.core.ast.*;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * @author Ocelot
 */
@ApiStatus.Internal
public final class MolangParser {

    public static Node parseTokens(MolangLexer.Token[] tokens) throws MolangSyntaxException {
        if (tokens.length == 0) {
            throw new MolangSyntaxException("Expected token");
        }
        return parseTokensUntil(new TokenReader(tokens), true, token -> false);
    }

    private static Node parseTokensUntil(TokenReader reader, boolean insertReturn, Predicate<MolangLexer.Token> filter) throws MolangSyntaxException {
        List<Node> nodes = new ArrayList<>(2);

        while (reader.canRead() && !filter.test(reader.peek())) {
            Node node = parseExpression(reader);
            nodes.add(node);

            if (reader.canRead()) {
                MolangLexer.Token token = reader.peek();
                if (token.type().isTerminating()) {
                    reader.skip();
                    continue;
                }
                if (filter.test(token)) {
                    break;
                }
                throw error("Trailing statement", reader);
            }
        }

        if (nodes.isEmpty()) {
            throw new MolangSyntaxException("Expected node");
        }
        if (insertReturn) {
            Node node = nodes.get(nodes.size() - 1);
            if (!(node instanceof ReturnNode)) {
                if (node instanceof OptionalValueNode setNode) {
                    node = setNode.withReturnValue();
                }
                nodes.set(nodes.size() - 1, new ReturnNode(node));
            }
        }
        if (nodes.size() == 1) {
            return nodes.get(0);
        }
        return new CompoundNode(nodes.toArray(Node[]::new));
    }

    // Parses a single token statement. Eg temp.a=4 or variable.test from variable.test * 2;
    private static Node parseNode(TokenReader reader) throws MolangSyntaxException {
        expectLength(reader, 1);

        MolangLexer.Token token = reader.peek();
        return switch (token.type()) {
            case RETURN -> {
                reader.skip();

                Node value = parseExpression(reader);
                // Skip ;
                if (reader.canRead() && reader.peek().type().isTerminating()) {
                    reader.skip();
                }
                // Expect end
                boolean scope = reader.canRead() && reader.peek().type() == MolangLexer.TokenType.RIGHT_BRACE;
                if (reader.canRead() && !scope) {
                    throw error("Trailing statement", reader);
                }
                if (value instanceof OptionalValueNode setNode) {
                    value = setNode.withReturnValue();
                }
                yield new ReturnNode(value);
            }
            case LOOP -> {
                reader.skip();
                expect(reader, MolangLexer.TokenType.LEFT_PARENTHESIS);
                reader.skip();

                Node iterations = parseTokensUntil(reader, false, t -> t.type() == MolangLexer.TokenType.COMMA);
                expect(reader, MolangLexer.TokenType.COMMA);
                reader.skip();

                Node body = parseTokensUntil(reader, false, t -> t.type() == MolangLexer.TokenType.RIGHT_PARENTHESIS);
                expect(reader, MolangLexer.TokenType.RIGHT_PARENTHESIS);
                reader.skip();

                // Ignore the top level scope since the loop is already a "scope"
                yield new LoopNode(iterations, body instanceof ScopeNode scopeNode ? scopeNode.node() : body);
            }
            case CONTINUE -> {
                reader.skip();
                yield new ContinueNode();
            }
            case BREAK -> {
                reader.skip();
                yield new BreakNode();
            }
            case IF -> {
                reader.skip();
                expect(reader, MolangLexer.TokenType.LEFT_PARENTHESIS);
                reader.skip();

                // if(condition)
                Node condition = parseExpression(reader);

                expect(reader, MolangLexer.TokenType.RIGHT_PARENTHESIS);
                reader.skip();

                Node branch = parseExpression(reader);
                if (reader.canRead(2) && reader.peek().type().isTerminating() && reader.peekAfter(1).type() == MolangLexer.TokenType.ELSE) {
                    reader.skip(2);
                    yield new TernaryOperationNode(condition, branch, parseExpression(reader));
                }

                // value ? left
                yield new BinaryConditionalNode(condition, branch);
            }
            case THIS -> {
                reader.skip();
                yield new ThisNode();
            }
            case TRUE -> {
                reader.skip();
                yield new ConstNode(1.0F);
            }
            case FALSE -> {
                reader.skip();
                yield new ConstNode(0.0F);
            }
            case NUMERAL -> {
                try {
                    // 3
                    float value = Integer.parseInt(reader.peek().value());
                    reader.skip();

                    // 3.14
                    if (reader.canRead() && reader.peek().type() == MolangLexer.TokenType.DOT) {
                        reader.skip();
                        expect(reader, MolangLexer.TokenType.NUMERAL);

                        String decimalString = reader.peek().value();
                        float decimal = Integer.parseInt(decimalString);
                        reader.skip();

                        if (decimal > 0) {
                            value += (float) (decimal / Math.pow(10, decimalString.length()));
                        }
                    }

                    yield new ConstNode(value);
                } catch (Exception e) {
                    e.printStackTrace();
                    throw error("Error parsing numeral", reader);
                }
            }
            case ALPHANUMERIC -> parseAlphanumeric(reader);
            case BINARY_OPERATION -> {
                switch (token.value()) {
                    case "-" -> {
                        if (!reader.canRead(2) || !reader.peekAfter(1).type().canNegate()) {
                            throw error("Cannot negate " + reader.peekAfter(1), reader);
                        }
                        reader.skip();
                        yield new BinaryOperationNode(BinaryOperation.MULTIPLY, new ConstNode(-1.0F), parseNode(reader));
                    }
                    case "+" -> {
                        if (!reader.canRead(2) || !reader.peekAfter(1).type().canNegate()) {
                            throw error("Cannot assign + to " + reader.peekAfter(1), reader);
                        }
                        reader.skip();
                        yield parseNode(reader);
                    }
                    default -> throw error("Expected +num or -num", reader);
                }
            }
            case LEFT_PARENTHESIS -> {
                reader.skip();
                Node node = parseExpression(reader);
                expect(reader, MolangLexer.TokenType.RIGHT_PARENTHESIS);
                reader.skip();
                yield node;
            }
            case LEFT_BRACE -> {
                reader.skip();
                Node node = parseTokensUntil(reader, false, t -> t.type() == MolangLexer.TokenType.RIGHT_BRACE);
                expect(reader, MolangLexer.TokenType.RIGHT_BRACE);
                reader.skip();
                yield new ScopeNode(node);
            }
            default -> throw error("Unexpected token", reader);
        };
    }

    // Parses a full expression by parsing each node inside
    public static Node parseExpression(TokenReader reader) throws MolangSyntaxException {
        Node result = parseNode(reader);
        while (reader.canRead()) {
            MolangLexer.Token token = reader.peek();
            if (token.type() == MolangLexer.TokenType.SEMICOLON || token.type().isOutOfScope()) {
                return result;
            }

            if (result instanceof OptionalValueNode setNode) {
                result = setNode.withReturnValue();
            }

            switch (token.type()) {
                case NUMERAL, ALPHANUMERIC, LEFT_PARENTHESIS, LEFT_BRACE -> {
                    if (result != null) {
                        throw error("Unexpected token", reader);
                    }
                    result = parseNode(reader);
                }
                case NULL_COALESCING -> {
                    reader.skip();
                    result = new BinaryOperationNode(BinaryOperation.NULL_COALESCING, result, parseNode(reader));
                }
                case EQUAL -> {
                    reader.skip();
                    expect(reader, MolangLexer.TokenType.EQUAL);
                    reader.skip();
                    result = new BinaryOperationNode(BinaryOperation.EQUALS, result, parseNode(reader));
                }
                case SPECIAL -> {
                    switch (token.value()) {
                        // obj.name&&...
                        case "&" -> {
                            expect(reader, MolangLexer.TokenType.SPECIAL, "&");
                            reader.skip(2);
                            result = new BinaryOperationNode(BinaryOperation.AND, result, parseNode(reader));
                        }
                        // obj.name||...
                        case "|" -> {
                            expect(reader, MolangLexer.TokenType.SPECIAL, "|");
                            reader.skip(2);
                            result = new BinaryOperationNode(BinaryOperation.OR, result, parseNode(reader));
                        }
                        // obj.name??... or obj.name?b...
                        case "?" -> {
                            reader.skip();

                            // value ? left : right
                            Node left = parseExpression(reader);
                            if (reader.canRead() && !reader.peek().type().isTerminating()) {
                                expect(reader, MolangLexer.TokenType.SPECIAL, ":");
                                reader.skip();
                                result = new TernaryOperationNode(result, left, parseExpression(reader));
                                break;
                            }

                            // value ? left
                            result = new BinaryConditionalNode(result, left);
                        }
                        case "!" -> {
                            reader.skip();

                            if (reader.peek().type() == MolangLexer.TokenType.EQUAL) {
                                reader.skip();
                                result = new BinaryOperationNode(BinaryOperation.NOT_EQUALS, result, parseNode(reader));
                                break;
                            }

                            if (result != null) {
                                throw error("Unexpected token", reader);
                            }
                            result = new NegateNode(parseNode(reader));
                        }
                        case ">" -> {
                            reader.skip();

                            if (reader.peek().type() == MolangLexer.TokenType.EQUAL) {
                                reader.skip();
                                result = new BinaryOperationNode(BinaryOperation.GREATER_EQUALS, result, parseNode(reader));
                                break;
                            }

                            result = new BinaryOperationNode(BinaryOperation.GREATER, result, parseNode(reader));
                        }
                        case "<" -> {
                            reader.skip();

                            if (reader.peek().type() == MolangLexer.TokenType.EQUAL) {
                                reader.skip();
                                result = new BinaryOperationNode(BinaryOperation.LESS_EQUALS, result, parseNode(reader));
                                break;
                            }

                            result = new BinaryOperationNode(BinaryOperation.LESS, result, parseNode(reader));
                        }
                        default -> {
                            return result;
                        }
                    }
                }
                case BINARY_OPERATION -> {
                    if (result == null) {
                        throw error("Unexpected token", reader);
                    }
                    result = parseBinaryExpression(result, reader);
                }
                default -> throw error("Unexpected token: " + token, reader);
            }
        }

        return result;
    }

    private static Node parseAlphanumeric(TokenReader reader) throws MolangSyntaxException {
        expectLength(reader, 2);

        // object.name
        String object = reader.peek().lowercaseValue();
        if ("t".equals(object)) {
            object = "temp";
        }

        reader.skip();
        expect(reader, MolangLexer.TokenType.DOT);
        reader.skip();

        expect(reader, MolangLexer.TokenType.ALPHANUMERIC);
        StringBuilder nameBuilder = new StringBuilder(reader.peek().lowercaseValue());
        reader.skip();
        while (reader.canRead()) {
            MolangLexer.Token token = reader.peek();
            if (!token.type().validVariableName()) {
                break;
            }
            nameBuilder.append(token.lowercaseValue());
            reader.skip();
        }

        String name = nameBuilder.toString();

        MathOperation mathOperation = parseMathOperation(object, name, reader);
        if (mathOperation != null && mathOperation.getParameters() == 0) {
            return new MathNode(mathOperation);
        }

        // obj.name
        if (!reader.canRead() || reader.peek().type().isTerminating()) {
            if (mathOperation != null) {
                throw error("Cannot get value of a math function", reader);
            }
            return new VariableGetNode(object, name);
        }

        MolangLexer.Token operand = reader.peek();

        // obj.name=...
        if (operand.type() == MolangLexer.TokenType.EQUAL) {
            // obj.name==...
            if (reader.canRead() && reader.peekAfter(1).type() == MolangLexer.TokenType.EQUAL) {
                // == will be handled by the next step
                return new VariableGetNode(object, name);
            }

            if (mathOperation != null) {
                throw error("Cannot set value of a math function", reader);
            }
            reader.skip();
            return new VariableSetNode(object, name, parseExpression(reader));
        }
        // obj.name++
        if (operand.type() == MolangLexer.TokenType.INCREMENT) {
            reader.skip();
            return new VariableSetNode(object, name, new BinaryOperationNode(BinaryOperation.ADD, new VariableGetNode(object, name), new ConstNode(1.0F)));
        }
        // obj.name--
        if (operand.type() == MolangLexer.TokenType.DECREMENT) {
            reader.skip();
            return new VariableSetNode(object, name, new BinaryOperationNode(BinaryOperation.SUBTRACT, new VariableGetNode(object, name), new ConstNode(1.0F)));
        }
        // obj.name*=, obj.name+=, obj.name-=, obj.name/=
        if (reader.canRead(2) && operand.type() == MolangLexer.TokenType.BINARY_OPERATION) {
            if (mathOperation != null) {
                throw error("Cannot set value of a math function", reader);
            }

            VariableGetNode left = new VariableGetNode(object, name);
            MolangLexer.Token secondOperand = reader.peekAfter(1);

            // +=, -=, *=, /=
            if (secondOperand.type() == MolangLexer.TokenType.EQUAL) {
                reader.skip(2);
                Node value = switch (operand.value()) {
                    case "-" -> new BinaryOperationNode(BinaryOperation.SUBTRACT, left, parseExpression(reader));
                    case "+" -> new BinaryOperationNode(BinaryOperation.ADD, left, parseExpression(reader));
                    case "*" -> new BinaryOperationNode(BinaryOperation.MULTIPLY, left, parseExpression(reader));
                    case "/" -> new BinaryOperationNode(BinaryOperation.DIVIDE, left, parseExpression(reader));
                    default -> throw error("Unexpected token", reader);
                };
                return new VariableSetNode(object, name, value);
            }
        }
        // obj.func(..
        if (operand.type() == MolangLexer.TokenType.LEFT_PARENTHESIS) {
            reader.skip();

            // obj.func()
            if (reader.peek().type() == MolangLexer.TokenType.RIGHT_PARENTHESIS) {
                reader.skip();
                return new FunctionNode(object, name);
            }

            // obj.func(a, b, ...)
            List<Node> parameters = new ArrayList<>();
            while (reader.canRead()) {
                parameters.add(parseExpression(reader));

                if (reader.peek().type() == MolangLexer.TokenType.COMMA) {
                    reader.skip();
                    continue;
                }

                expect(reader, MolangLexer.TokenType.RIGHT_PARENTHESIS);
                reader.skip();

                // Validate number of parameters for math functions
                if (mathOperation != null) {
                    if (mathOperation.getParameters() != parameters.size()) {
                        throw error("Expected " + mathOperation.getParameters() + " parameters, got " + parameters.size(), reader);
                    }
                    return new MathNode(mathOperation, parameters.toArray(Node[]::new));
                }

                return new FunctionNode(object, name, parameters.toArray(Node[]::new));
            }
            expectLength(reader, 1);
        }

        return new VariableGetNode(object, name);
    }

    private static MathOperation parseMathOperation(String object, String name, TokenReader reader) throws MolangSyntaxException {
        if (!"math".equalsIgnoreCase(object)) {
            return null;
        }

        for (MathOperation operation : MathOperation.values()) {
            if (operation.getName().equalsIgnoreCase(name)) {
                return operation;
            }
        }
        throw error("Unknown math function: " + name, reader);
    }

    private static Node parseBinaryExpression(Node left, TokenReader reader) throws MolangSyntaxException {
        MolangLexer.Token token = reader.peek();
        switch (token.value()) {
            case "+" -> {
                reader.skip();
                return new BinaryOperationNode(BinaryOperation.ADD, left, parseTerm(reader));
            }
            case "-" -> {
                reader.skip();
                return new BinaryOperationNode(BinaryOperation.SUBTRACT, left, parseTerm(reader));
            }
            case "*" -> {
                reader.skip();
                return new BinaryOperationNode(BinaryOperation.MULTIPLY, left, parseNode(reader));
            }
            case "/" -> {
                reader.skip();
                return new BinaryOperationNode(BinaryOperation.DIVIDE, left, parseNode(reader));
            }
        }
        return left;
    }

    private static Node parseTerm(TokenReader reader) throws MolangSyntaxException {
        Node left = parseNode(reader);
        if (!reader.canRead()) {
            return left;
        }

        MolangLexer.Token token = reader.peek();
        if (token.type() == MolangLexer.TokenType.BINARY_OPERATION) {
            switch (token.value()) {
                case "*" -> {
                    reader.skip();
                    return new BinaryOperationNode(BinaryOperation.MULTIPLY, left, parseNode(reader));
                }
                case "/" -> {
                    reader.skip();
                    return new BinaryOperationNode(BinaryOperation.DIVIDE, left, parseNode(reader));
                }
            }
        }
        return left;
    }

    public static void expect(TokenReader reader, MolangLexer.TokenType token) throws MolangSyntaxException {
        if (!reader.canRead() || reader.peek().type() != token) {
            throw error("Expected " + token, reader);
        }
    }

    public static void expect(TokenReader reader, MolangLexer.TokenType token, String value) throws MolangSyntaxException {
        expect(reader, token);
        if (!value.equals(reader.peek().value())) {
            throw error("Expected " + value, reader);
        }
    }

    public static void expectLength(TokenReader reader, int amount) throws MolangSyntaxException {
        if (!reader.canRead(amount)) {
            throw new MolangSyntaxException("Trailing statement", reader.getString(), reader.getString().length());
        }
    }

    public static MolangSyntaxException error(String error, TokenReader reader) {
        return new MolangSyntaxException(error, reader.getString(), reader.getCursorOffset());
    }
}
