package io.github.ocelot.molangcompiler.api;

import io.github.ocelot.molangcompiler.api.exception.MolangException;
import io.github.ocelot.molangcompiler.api.exception.MolangSyntaxException;
import io.github.ocelot.molangcompiler.api.object.MolangObject;
import io.github.ocelot.molangcompiler.core.MolangJavaFunctionContext;
import io.github.ocelot.molangcompiler.core.compiler.Dynamic2MolangExceptionType;
import io.github.ocelot.molangcompiler.core.compiler.DynamicMolangExceptionType;
import io.github.ocelot.molangcompiler.core.compiler.SimpleMolangExceptionType;
import io.github.ocelot.molangcompiler.core.compiler.StringReader;
import io.github.ocelot.molangcompiler.core.node.*;
import io.github.ocelot.molangcompiler.core.object.MolangMath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Compiles a {@link MolangExpression} from a string input.
 *
 * @author Ocelot
 * @since 1.0.0
 */
public class MolangCompiler
{
    private static final SimpleMolangExceptionType UNEXPECTED_TOKEN = new SimpleMolangExceptionType("Unexpected token");
    private static final DynamicMolangExceptionType INVALID_KEYWORD = new DynamicMolangExceptionType(obj -> "Invalid keyword: " + obj);
    private static final DynamicMolangExceptionType EXPECTED = new DynamicMolangExceptionType(obj -> "Expected " + obj);
    private static final SimpleMolangExceptionType TRAILING_STATEMENT = new SimpleMolangExceptionType("Trailing statement");
    private static final Dynamic2MolangExceptionType NOT_ENOUGH_PARAMETERS = new Dynamic2MolangExceptionType((obj, obj2) -> "Not enough parameters. Expected at least " + obj + ", got " + obj2);
    private static final Dynamic2MolangExceptionType TOO_MANY_PARAMETERS = new Dynamic2MolangExceptionType((obj, obj2) -> "Too many parameters. Expected at most " + obj + ", got " + obj2);

    /**
     * Whether to reduce math to constant values if possible. E.g. <code>4 * 4 + 2</code> would become <code>18</code>. This should almost always be on.
     */
    public static final int REDUCE_FLAG = 0b00000001;
    /**
     * Whether to check for 'this' keyword.
     */
    private static final int CHECK_THIS_FLAG = 0b00000010;
    /**
     * Whether to check for variables.
     */
    private static final int CHECK_VARIABLE_FLAG = 0b00000100;
    /**
     * Whether to check for methods.
     */
    private static final int CHECK_METHOD_FLAG = 0b00001000;
    /**
     * Whether to check for math operations.
     */
    private static final int CHECK_OPERATORS_FLAG = 0b00010000;
    /**
     * Whether to check for scopes. Eg {}.
     */
    private static final int CHECK_SCOPE_FLAG = 0b00100000;

    private static final MolangEnvironment ENVIRONMENT = new CompileEnvironment();
    private static final List<Character> MATH_OPERATORS = Arrays.asList('(', ')', '*', '/', '+', '-');

    /**
     * Compiles a {@link MolangExpression} from the specified string input.
     *
     * @param input The data to compile
     * @return The compiled expression
     * @throws MolangSyntaxException If any error occurs
     */
    public static MolangExpression compile(String input) throws MolangSyntaxException
    {
        return compile(input, REDUCE_FLAG);
    }

    /**
     * Compiles a {@link MolangExpression} from the specified string input. Use {@link #compile(String)} for the best general settings.
     *
     * @param input The data to compile
     * @param flags Additional flags to use when compiling
     * @return The compiled expression
     * @throws MolangSyntaxException If any error occurs
     */
    public static MolangExpression compile(String input, int flags) throws MolangSyntaxException
    {
        if (input.isEmpty())
            throw UNEXPECTED_TOKEN.create();

        // Set initial flags
        if (input.contains("."))
        {
            flags |= CHECK_VARIABLE_FLAG;
            if (input.contains("("))
                flags |= CHECK_METHOD_FLAG | CHECK_OPERATORS_FLAG;
        }
        if (input.contains("this"))
            flags |= CHECK_THIS_FLAG;
        if (!checkFlag(flags, CHECK_OPERATORS_FLAG))
        {
            for (char operator : MATH_OPERATORS)
            {
                if (input.indexOf(operator) != -1)
                {
                    flags |= CHECK_OPERATORS_FLAG;
                    break;
                }
            }
        }

        return parseGroup(input, flags, true);
    }

    private static MolangExpression parseGroup(String input, int flags, boolean strictSyntax) throws MolangSyntaxException
    {
        List<MolangExpression> expressions = new ArrayList<>();
        StringReader reader = new StringReader(input);
        reader.skipWhitespace();
        if (!reader.canRead())
            throw UNEXPECTED_TOKEN.create();

        if ((strictSyntax || !input.contains("return")) && input.chars().filter(c -> c == ';').count() <= 1)
            return parseExpression(new StringReader(input.replaceAll(";", "")), flags, true, true);

        int start = 0;
        int scopeStart = 0;
        int scope = 0;
        boolean startScope = false;
        while (reader.canRead())
        {
            if (reader.peek() == '{')
            {
                if (scopeStart == reader.getCursor())
                    startScope = true;
                scope++;
                flags |= CHECK_SCOPE_FLAG;
            }
            else if (reader.peek() == '}')
            {
                scope--;
                if (scope < 0)
                    throw TRAILING_STATEMENT.createWithContext(reader);
                if (scope == 0 && startScope)
                {
                    startScope = false;
                    reader.skip();
                    expressions.add(parseExpression(new StringReader(input.substring(start, reader.getCursor())), flags, false, true));
                    start = reader.getCursor();
                }
            }
            else if (scope == 0 && reader.peek() == ';')
            {
                expressions.add(parseExpression(new StringReader(input.substring(start, reader.getCursor())), flags, false, true));
                start = reader.getCursor() + 1;
                reader.skip();
                reader.skipWhitespace();
                scopeStart = reader.getCursor();
                continue;
            }

            reader.skip();
            reader.skipWhitespace();
        }

        if (expressions.isEmpty())
        {
            if (strictSyntax)
                throw UNEXPECTED_TOKEN.createWithContext(reader);
            return parseExpression(new StringReader(input.replaceAll(";", "")), flags, false, true);
        }

        return expressions.size() == 1 ? expressions.get(0) : new MolangCompoundNode(expressions.toArray(new MolangExpression[0]));
    }

    private static MolangExpression parseExpression(StringReader reader, int flags, boolean simple, boolean allowMath) throws MolangSyntaxException
    {
        reader.skipWhitespace(); // Skip potential spaces or tabs before '=', '*', etc
        if (!reader.canRead())
            throw UNEXPECTED_TOKEN.createWithContext(reader);

        // Check for scope
        if (checkFlag(flags, CHECK_SCOPE_FLAG) && reader.peek() == '{')
        {
            reader.skip();
            int start = reader.getCursor();
            int scope = 1;
            while (reader.canRead())
            {
                if (reader.peek() == '{')
                {
                    scope++;
                }
                else if (reader.peek() == '}')
                {
                    scope--;
                    if (scope < 0)
                        break;
                    if (scope == 0)
                    {
                        reader.skip();
                        return new MolangScopeNode(parseGroup(reader.getString().substring(start, reader.getCursor() - 1), flags, false));
                    }
                }
                reader.skip();
            }
            throw TRAILING_STATEMENT.createWithContext(reader);
        }

        // Check for return
        if (reader.getRemaining().startsWith("return"))
        {
            reader.skip(6);
            if (simple)
                throw UNEXPECTED_TOKEN.createWithContext(reader);
            MolangExpression expression = parseExpression(reader, flags, true, allowMath);
            if (reader.canRead())
                throw TRAILING_STATEMENT.createWithContext(reader);
            return expression;
        }

        // Check for math. This will not happen if reading from math because operators are removed
        if (checkFlag(flags, CHECK_OPERATORS_FLAG) && !reader.getRemaining().contains("=") && !reader.getRemaining().contains("?") && !reader.getRemaining().contains(":") && allowMath)
        {
            for (char operator : MATH_OPERATORS)
            {
                if (reader.getRemaining().chars().anyMatch(a -> a == operator))
                {
                    return compute(reader, flags);
                }
            }
        }

        String[] currentKeyword = parseKeyword(reader, simple); // This handles 'abc.def' etc
        String fullWord = currentKeyword[0] + (currentKeyword.length > 1 ? "." + currentKeyword[1] : "");

        // Check for 'this' keyword
        if (checkFlag(flags, CHECK_THIS_FLAG) && "this".equals(fullWord))
        {
            reader.skipWhitespace();
            if (reader.canRead() && reader.peek() != '?' && reader.peek() != ':' && !MATH_OPERATORS.contains(reader.peek()))
                throw TRAILING_STATEMENT.createWithContext(reader);
            return parseCondition(reader, new MolangThisNode(), flags, allowMath);
        }

        // Check for 'true' keyword
        if ("true".equals(fullWord) || "false".equals(fullWord))
        {
            reader.skipWhitespace();
            if (reader.canRead() && reader.peek() != '?' && reader.peek() != ':' && !MATH_OPERATORS.contains(reader.peek()))
                throw TRAILING_STATEMENT.createWithContext(reader);
            return parseCondition(reader, MolangExpression.of("true".equals(fullWord)), flags, allowMath);
        }

        // Check for number
        if (isNumber(fullWord))
        {
            reader.skipWhitespace();
            if (reader.canRead() && reader.peek() != '?' && reader.peek() != ':' && !MATH_OPERATORS.contains(reader.peek()))
                throw TRAILING_STATEMENT.createWithContext(reader);
            return parseCondition(reader, MolangExpression.of(Float.parseFloat(fullWord)), flags, allowMath);
        }

        // methods and params require at least both parts
        if (currentKeyword.length <= 1 || currentKeyword[0].isEmpty())
            throw UNEXPECTED_TOKEN.createWithContext(reader);

        // objects are not allowed to start with numbers
        if (isNumber(currentKeyword[0].substring(0, 1)))
            throw UNEXPECTED_TOKEN.createWithContext(reader);

        // Check for methods
        if (checkFlag(flags, CHECK_METHOD_FLAG) && reader.canRead() && reader.peek() == '(')
        {
            int start = reader.getCursor();
            reader.skip();
            MolangExpression[] parameters = null;
            int parentheses = 0;
            while (reader.canRead() && start != -1)
            {
                if (reader.peek() == '(')
                    parentheses++;
                if (reader.peek() == ')')
                {
                    if (parentheses > 0)
                    {
                        parentheses--;
                        reader.skip();
                        continue;
                    }
                    if (reader.getCursor() == start)
                    {
                        parameters = new MolangExpression[0];
                    }
                    else
                    {
                        String[] parameterStrings = reader.getRead().substring(start).split(",");
                        parameters = new MolangExpression[parameterStrings.length];
                        for (int i = 0; i < parameterStrings.length; i++)
                        {
                            parameters[i] = parseExpression(new StringReader(parameterStrings[i]), flags, true, true);
                        }
                    }

                    start = -1;
                }
                reader.skip();
            }
            if (start != -1)
                throw EXPECTED.createWithContext(reader, ')');

            reader.skipWhitespace();
            return parseCondition(reader, parseMethod(currentKeyword, parameters, flags), flags, allowMath);
        }
        else if (checkFlag(flags, CHECK_VARIABLE_FLAG))
        {
            // Check for variables
            reader.skipWhitespace();
            if (reader.canRead())
            {
                if (reader.peek() == '=')
                {
                    reader.skip();
                    MolangExpression expression = parseExpression(reader, flags, true, allowMath);
                    return new MolangSetVariableNode(currentKeyword[0], currentKeyword[1], expression);
                }
            }
            if ("math".equalsIgnoreCase(currentKeyword[0])) // Attempt to reduce math constants
            {
                MolangMath.MathFunction function = MolangMath.MathFunction.byName(currentKeyword[1]);
                if (function == null || function.getOp() != null)
                    throw INVALID_KEYWORD.create(currentKeyword[1]);
                if (checkFlag(flags, REDUCE_FLAG))
                    return function.getExpression();
            }
            return parseCondition(reader, new MolangGetVariableNode(currentKeyword[0], currentKeyword[1]), flags, allowMath);
        }
        throw TRAILING_STATEMENT.createWithContext(reader);
    }

    private static MolangExpression parseCondition(StringReader reader, MolangExpression expression, int flags, boolean allowMath) throws MolangSyntaxException
    {
        if (reader.canRead() && reader.peek() == '?')
        {
            reader.skip();
            reader.skipWhitespace();

            int start = reader.getCursor();
            while (reader.canRead() && reader.peek() != ':')
                reader.skip();

            if (!reader.canRead())
                throw TRAILING_STATEMENT.createWithContext(reader);

            MolangExpression first = parseExpression(new StringReader(reader.getRead().substring(start)), flags, true, allowMath);
            if (!reader.canRead())
                throw TRAILING_STATEMENT.createWithContext(reader);
            reader.skip();

            MolangExpression branch = parseExpression(reader, flags, true, allowMath);
            MolangExpression condition = new MolangConditionalNode(expression, first, branch);
            if (checkFlag(flags, REDUCE_FLAG) && expression instanceof MolangConstantNode)
            {
                try
                {
                    return expression.resolve(ENVIRONMENT) != 0.0 ? first : branch;
                }
                catch (MolangException e)
                {
                    // This should literally never happen
                    e.printStackTrace();
                }
            }
            return condition;
        }
        return expression;
    }

    private static String[] parseKeyword(StringReader reader, boolean simple) throws MolangSyntaxException
    {
        List<String> keywords = new LinkedList<>();
        int start = reader.getCursor();
        while (reader.canRead() && isValidKeywordChar(reader.peek()))
        {
            if (reader.peek() == '.' && keywords.isEmpty())
            {
                keywords.add(reader.getRead().substring(start));
                reader.skip();
                start = reader.getCursor();
            }
            if (!reader.canRead())
                throw UNEXPECTED_TOKEN.createWithContext(reader);
            reader.skip();
        }
        if (start < reader.getCursor())
            keywords.add(reader.getRead().substring(start));
        if (keywords.stream().allMatch(String::isEmpty))
        {
            if (simple)
                return new String[0];
            throw UNEXPECTED_TOKEN.createWithContext(reader);
        }
        return keywords.toArray(new String[0]);
    }

    private static boolean hasMethod(StringReader reader)
    {
        int start = reader.getCursor();
        int parenthesis = -1;
        while (reader.canRead() && (isValidKeywordChar(reader.peek()) || reader.peek() == '(' || reader.peek() == ')' || parenthesis >= 0))
        {
            if (reader.peek() == '(')
            {
                if (parenthesis == -1)
                    parenthesis = 0;
                parenthesis++;
            }
            if (reader.peek() == ')')
            {
                if (parenthesis == 0)
                    break;
                parenthesis--;
            }
            if (!reader.canRead())
            {
                reader.setCursor(start);
                return false;
            }
            reader.skip();
        }
        boolean success = start < reader.getCursor();
        reader.setCursor(start);
        return success && parenthesis == 0;
    }

    private static MolangExpression parseMethod(String[] methodName, MolangExpression[] parameters, int flags) throws MolangSyntaxException
    {
        // Special case for math to check if it's valid
        if ("math".equalsIgnoreCase(methodName[0]))
        {
            MolangMath.MathFunction function = MolangMath.MathFunction.byName(methodName[1] + "$" + parameters.length);
            if (function == null)
            {
                function = MolangMath.MathFunction.byName(methodName[1]);
                if (function == null)
                    throw INVALID_KEYWORD.create(methodName[1]);
            }
            if (function.getOp() == null) // Not a function
                throw UNEXPECTED_TOKEN.create();
            if (function.getParameters() >= 0)
            {
                if (parameters.length < function.getParameters())
                    throw NOT_ENOUGH_PARAMETERS.create(function.getParameters(), parameters.length);
                if (parameters.length > function.getParameters())
                    throw TOO_MANY_PARAMETERS.create(function.getParameters(), parameters.length);
            }

            if (checkFlag(flags, REDUCE_FLAG) && function.canOptimize())
            {
                // Math functions are constant so these can be compiled down to raw numbers if all parameters are constants
                boolean reduceFunction = true;
                for (int i = 0; i < parameters.length; i++)
                {
                    if (parameters[i] instanceof MolangConstantNode)
                        continue;
                    try
                    {
                        parameters[i] = MolangExpression.of(parameters[i].resolve(ENVIRONMENT));
                    }
                    catch (MolangException e)
                    {
                        // The parameter is runtime dependent, so the entire function is blocked from being computed.
                        // Parameters can still be computed so there is no reason to stop trying to reduce
                        reduceFunction = false;
                    }
                }

                if (reduceFunction)
                {
                    try
                    {
                        return MolangExpression.of(function.getOp().resolve(new MolangJavaFunctionContext(ENVIRONMENT, parameters)));
                    }
                    catch (MolangException e)
                    {
                        // Something went horribly wrong with the above checks
                        e.printStackTrace();
                    }
                }
            }
        }
        // Other functions may or may not work, the runtime determines if they will
        return new MolangInvokeFunctionNode(methodName[0], methodName[1], parameters);
    }

    // Based on https://stackoverflow.com/questions/3422673/how-to-evaluate-a-math-expression-given-in-string-form
    // Stack Overflow! Every programmer's best friend!
    private static MolangExpression compute(StringReader reader, int flags) throws MolangSyntaxException
    {
        return new Object()
        {
            private boolean canReduce = checkFlag(flags, REDUCE_FLAG);

            boolean accept(int charToEat)
            {
                reader.skipWhitespace();
                if (reader.canRead() && reader.peek() == charToEat)
                {
                    reader.skip();
                    return true;
                }
                return false;
            }

            MolangExpression parse() throws MolangSyntaxException
            {
                reader.skipWhitespace();
                MolangExpression x = parseExpression();
                reader.skipWhitespace();
                if (reader.canRead())
                    throw TRAILING_STATEMENT.createWithContext(reader);

                // Reduction is impossible because of runtime dependence
                if (!this.canReduce)
                    return x;

                // Attempt to reduce if possible
                try
                {
                    return MolangExpression.of(x.resolve(ENVIRONMENT));
                }
                catch (MolangException e)
                {
                    // Something went wrong with the checks
                    e.printStackTrace();
                }

                return x;
            }

            MolangExpression parseExpression() throws MolangSyntaxException
            {
                MolangExpression x = parseTerm();
                while (true)
                {
                    if (accept('+'))
                    {
                        x = new MolangMathOperatorNode(MolangMathOperatorNode.MathOperation.ADD, x, parseTerm()); // addition
                    }
                    else if (accept('-'))
                    {
                        x = new MolangMathOperatorNode(MolangMathOperatorNode.MathOperation.SUBTRACT, x, parseTerm()); // subtraction
                    }
                    else
                    {
                        return x;
                    }
                }
            }

            MolangExpression parseTerm() throws MolangSyntaxException
            {
                MolangExpression x = parseFactor();
                while (true)
                {
                    boolean accept = accept('*');
                    if (accept)
                    {
                        x = new MolangMathOperatorNode(MolangMathOperatorNode.MathOperation.MULTIPLY, x, parseFactor()); // multiplication
                    }
                    else if (accept('/'))
                    {
                        x = new MolangMathOperatorNode(MolangMathOperatorNode.MathOperation.DIVIDE, x, parseFactor()); // division
                    }
                    else
                    {
                        return x;
                    }
                }
            }

            MolangExpression parseFactor() throws MolangSyntaxException
            {
                if (accept('+'))
                    return parseFactor(); // unary plus
                if (accept('-'))
                    return new MolangMathOperatorNode(MolangMathOperatorNode.MathOperation.MULTIPLY, parseFactor(), MolangExpression.of(-1)); // unary minus

                if (accept('('))
                {
                    MolangExpression expression = parseExpression();
                    accept(')');
                    return expression;
                }

                reader.skipWhitespace();
                int start = reader.getCursor();
                int parentheses = 0;
                boolean hasMethod = hasMethod(reader);
                while (reader.canRead() && (Character.isWhitespace(reader.peek()) || !MATH_OPERATORS.contains(reader.peek()) || hasMethod))
                {
                    if (reader.peek() == '(')
                    {
                        parentheses++;
                    }
                    if (reader.peek() == ')' && hasMethod)
                    {
                        parentheses--;
                        if (parentheses == 0)
                        {
                            reader.skip();
                            break;
                        }
                    }
                    reader.skip();
                }

                MolangExpression expression = MolangCompiler.parseExpression(new StringReader(reader.getRead().substring(start)), flags, true, false);
                if (!checkFlag(flags, REDUCE_FLAG))
                    return expression;
                if (this.canReduce && (expression instanceof MolangSetVariableNode || expression instanceof MolangInvokeFunctionNode || expression instanceof MolangGetVariableNode || expression instanceof MolangThisNode))
                    this.canReduce = false;
                return expression;
            }
        }.parse();
    }

    private static boolean checkFlag(int flags, int flag)
    {
        return (flags & flag) > 0;
    }

    private static boolean isValidKeywordChar(final char c)
    {
        return c >= '0' && c <= '9'
                || c >= 'A' && c <= 'Z'
                || c >= 'a' && c <= 'z'
                || c == '_' || c == '.';
    }

    private static boolean isNumber(String input)
    {
        if (input.isEmpty())
            return false;
        if (input.charAt(input.length() - 1) == '.')
            return false;
        if (input.charAt(0) == '-')
        {
            if (input.length() == 1)
                return false;
            return withDecimalsParsing(input, 1);
        }
        return withDecimalsParsing(input, 0);
    }

    private static boolean withDecimalsParsing(String str, int beginIndex)
    {
        int decimalPoints = 0;
        for (int i = beginIndex; i < str.length(); i++)
        {
            boolean isDecimalPoint = str.charAt(i) == '.';
            if (isDecimalPoint)
                decimalPoints++;
            if (decimalPoints > 1)
                return false;
            if (!isDecimalPoint && !Character.isDigit(str.charAt(i)))
                return false;
        }
        return true;
    }

    private static class CompileEnvironment implements MolangEnvironment
    {
        @Override
        public void loadParameter(int index, MolangExpression expression) throws MolangException
        {
            throw new MolangException("Invalid Call");
        }

        @Override
        public void clearParameters() throws MolangException
        {
            throw new MolangException("Invalid Call");
        }

        @Override
        public float getThis() throws MolangException
        {
            throw new MolangException("Invalid Call");
        }

        @Override
        public MolangObject get(String name) throws MolangException
        {
            throw new MolangException("Invalid Call");
        }

        @Override
        public MolangExpression getParameter(int parameter) throws MolangException
        {
            throw new MolangException("Invalid Call");
        }

        @Override
        public boolean hasParameter(int parameter) throws MolangException
        {
            throw new MolangException("Invalid Call");
        }
    }
}
