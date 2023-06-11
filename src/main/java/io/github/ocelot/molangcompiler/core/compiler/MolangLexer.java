package io.github.ocelot.molangcompiler.core.compiler;

import io.github.ocelot.molangcompiler.api.exception.MolangSyntaxException;
import io.github.ocelot.molangcompiler.core.ast.*;
import io.github.ocelot.molangcompiler.core.util.TokenReader;

import java.util.ArrayList;
import java.util.List;

public class MolangLexer {

    public static Node parseTokens(MolangTokenizer.Token[] tokens) throws MolangSyntaxException {
        if (tokens.length == 0) {
            throw new MolangSyntaxException("Expected token");
        }

        List<Node> nodes = new ArrayList<>(2);

        TokenReader reader = new TokenReader(tokens);
        while (reader.canRead()) {
            Node node = parseExpression(reader);
            if (reader.canRead()) {
                expect(reader, MolangTokenizer.TokenType.SEMICOLON);
            }
            nodes.add(node);
        }

        if (nodes.isEmpty()) {
            throw new MolangSyntaxException("Expected node");
        }
        return nodes.size() == 1 ? nodes.get(0) : new CompoundNode(nodes.toArray(Node[]::new));
    }

    private static Node parseExpression(TokenReader reader) throws MolangSyntaxException {
        if (!reader.canRead()) {
            throw error("Expected token", reader);
        }

        MolangTokenizer.Token token = reader.peek();
        switch (token.type()) {
            case NUMERAL -> {
            }
            case ALPHANUMERIC -> {
                reader.skip();
                return parseAlphanumeric(token, reader);
            }
            case SPECIAL -> {
                switch (token.value()) {
                    case "-" -> {

                    }
                    case "+" -> {

                    }
                    case "*" -> {

                    }
                    case "/" -> {

                    }
                    case "<" -> {

                    }
                    case ">" -> {

                    }
                    case "&" -> {

                    }
                    case "|" -> {

                    }
                    case "!" -> {

                    }
                    case "?" -> {

                    }
                }
            }
            case LEFT_PARENTHESIS -> {
                reader.skip();
                Node node = parseExpression(reader);
                expect(reader, MolangTokenizer.TokenType.RIGHT_PARENTHESIS);
                reader.skip();
                return node;
            }
            case LEFT_BRACE -> {
                reader.skip();
                Node node = parseExpression(reader);
                expect(reader, MolangTokenizer.TokenType.RIGHT_BRACE);
                reader.skip();
                return new ScopeNode(node);
            }
        }

        throw error("", reader);
    }

    private static Node parseAlphanumeric(MolangTokenizer.Token word, TokenReader reader) throws MolangSyntaxException {
        expectLength(reader, 2);

        String object = word.value();

        expect(reader, MolangTokenizer.TokenType.DOT);
        reader.skip();

        expect(reader, MolangTokenizer.TokenType.ALPHANUMERIC);
        String name = reader.peek().value();
        reader.skip();

        MathOperation mathOperation = parseMathOperation(object, name, reader);
        if (!reader.canRead()) {
            if (mathOperation != null) {
                if (mathOperation.getParameters() == 0) {
                    return new MathNode(mathOperation);
                }
                throw error("Cannot get value of a math function", reader);
            }
            return new VariableGetNode(object, name);
        }

        MolangTokenizer.Token operand = reader.peek();
        reader.skip();

        if (mathOperation == null) {
            // obj.name=...
            if (operand.type() == MolangTokenizer.TokenType.EQUAL) {
                return new VariableSetNode(object, name, parseExpression(reader));
            }
            // obj.name*...
            if (operand.type() == MolangTokenizer.TokenType.SPECIAL) {
                boolean assign = reader.canRead() && reader.peek().type() == MolangTokenizer.TokenType.EQUAL;
                if (assign) {
                    reader.skip();
                }

                VariableGetNode left = new VariableGetNode(object, name);
                Node operation = switch (operand.value()) {
                    case "-" -> parseBinaryOperation(reader, left, BinaryOperation.SUBTRACT, assign);
                    case "+" -> parseBinaryOperation(reader, left, BinaryOperation.ADD, assign);
                    case "*" -> parseBinaryOperation(reader, left, BinaryOperation.MULTIPLY, assign);
                    case "/" -> parseBinaryOperation(reader, left, BinaryOperation.DIVIDE, assign);
                    default -> null;
                };

                // *=, +=, etc
                if (operation != null) {
                    return assign ? new VariableSetNode(object, name, operation) : operation;
                }

                if (!assign) {
                    return parseSpecial(reader, left, operand);
                }
            }
        }
        // obj.func(..
        if (operand.type() == MolangTokenizer.TokenType.LEFT_PARENTHESIS) {
            if (mathOperation != null && mathOperation.getParameters() == 0) {
                throw error("Unexpected token", reader);
            }

            // obj.func()
            if (reader.peek().type() == MolangTokenizer.TokenType.RIGHT_PARENTHESIS) {
                reader.skip();
                return new FunctionNode(object, name);
            }

            // obj.func(a, b, ...)
            List<Node> parameters = new ArrayList<>();
            while (reader.canRead()) {
                parameters.add(parseExpression(reader));

                if (reader.peek().type() == MolangTokenizer.TokenType.COMMA) {
                    reader.skip();
                    continue;
                }

                expect(reader, MolangTokenizer.TokenType.RIGHT_PARENTHESIS);
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
        }

        throw error("Unexpected token", reader);
    }

    // Parses left &&..., left ??..., left ? right, left ? right : else
    private static Node parseSpecial(TokenReader reader, Node value, MolangTokenizer.Token operand) throws MolangSyntaxException {
        switch (operand.value()) {
            // obj.name&&...
            case "&" -> {
                expect(reader, MolangTokenizer.TokenType.SPECIAL, "&");
                reader.skip();
                return new BinaryOperationNode(BinaryOperation.AND, value, parseExpression(reader));
            }
            // obj.name??... or obj.name?b...
            case "?" -> {
                MolangTokenizer.Token token = reader.peek();
                reader.skip();

                // ??
                if (token.type() == MolangTokenizer.TokenType.SPECIAL && "?".equals(token.value())) {
                    return new BinaryOperationNode(BinaryOperation.NULL_COALESCING, value, parseExpression(reader));
                }

                Node left = parseExpression(reader);
                if (reader.canRead()) {
                    expect(reader, MolangTokenizer.TokenType.SPECIAL, ":");
                    reader.skip();
                    return new TernaryOperationNode(value, left, parseExpression(reader));
                }

                return new BinaryConditionalNode(value, left);
            }
        }

        throw error("Unexpected token", reader);
    }

    private static Node parseBinaryOperation(TokenReader reader, VariableGetNode left, BinaryOperation operation, boolean assign) throws MolangSyntaxException {
        if (!assign && reader.canRead()) {
            MolangTokenizer.Token token = reader.peek();

            // ++, --, etc
            if (token.type() == MolangTokenizer.TokenType.SPECIAL) {
                expect(reader, MolangTokenizer.TokenType.SPECIAL, operation.getValue());
                reader.skip();
                return new VariableSetNode(left.object(), left.name(), new BinaryOperationNode(operation, left, new ConstNode(1.0F)));
            }
        }

        return new BinaryOperationNode(operation, left, parseExpression(reader));
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

    private static void expect(TokenReader reader, MolangTokenizer.TokenType token) throws MolangSyntaxException {
        if (!reader.canRead() || reader.peek().type() != token) {
            throw error("Expected " + token, reader);
        }
    }

    private static void expect(TokenReader reader, MolangTokenizer.TokenType token, String value) throws MolangSyntaxException {
        expect(reader, token);
        if (!value.equals(reader.peek().value())) {
            throw error("Expected " + value, reader);
        }
    }

    private static void expectLength(TokenReader reader, int amount) throws MolangSyntaxException {
        if (!reader.canRead(amount)) {
            throw new MolangSyntaxException("Trailing statement", reader.getString(), reader.getString().length());
        }
    }

    private static MolangSyntaxException error(String error, TokenReader reader) {
        return new MolangSyntaxException(error, reader.getString(), reader.getCursorOffset());
    }
}
