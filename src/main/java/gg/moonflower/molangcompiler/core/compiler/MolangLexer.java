package gg.moonflower.molangcompiler.core.compiler;

import gg.moonflower.molangcompiler.api.exception.MolangSyntaxException;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Ocelot
 */
@ApiStatus.Internal
public final class MolangLexer {

    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("[\n\t]");

    public static Token[] createTokens(String input) throws MolangSyntaxException {
        StringReader reader = new StringReader(WHITESPACE_PATTERN.matcher(input).replaceAll(""));
        List<Token> tokens = new ArrayList<>();

        while (reader.canRead()) {
            reader.skipWhitespace();
            Token token = getToken(reader);
            if (token != null) {
                if (!tokens.isEmpty()) {
                    Token lastToken = tokens.get(tokens.size() - 1);
                    // Insert semicolon after scopes
                    if (lastToken.type == TokenType.RIGHT_BRACE && token.type != TokenType.SEMICOLON) {
                        tokens.add(new Token(TokenType.SEMICOLON, ";"));
                    }
                }
                tokens.add(token);
                continue;
            }

            throw new MolangSyntaxException("Unknown Token", reader.getString(), reader.getCursor());
        }

        return tokens.toArray(Token[]::new);
    }

    private static Token getToken(StringReader reader) {
        String word = reader.getString().substring(reader.getCursor());
        for (TokenType type : TokenType.values()) {
            Matcher matcher = type.pattern.matcher(word);
            if (matcher.find() && matcher.start() == 0) {
                reader.skip(matcher.end());
                return new Token(type, word.substring(0, matcher.end()));
            }
        }

        return null;
    }

    public record Token(TokenType type, String value) {
        public String lowercaseValue() {
            return this.value.toLowerCase(Locale.ROOT);
        }

        @Override
        public String toString() {
            return this.type + "[" + this.value + "]";
        }
    }

    public enum TokenType {
        RETURN("return"),
        LOOP("loop"),
        CONTINUE("continue"),
        BREAK("break"),
        IF("if"),
        ELSE("else"),
        THIS("this"),
        TRUE("true"),
        FALSE("false"),
        NUMERAL("\\d+"),
        ALPHANUMERIC("[A-Za-z_][A-Za-z0-9_]*"),
        NULL_COALESCING("\\?\\?"),
        INCREMENT("\\+\\+"),
        DECREMENT("\\-\\-"),
        SPECIAL("[<>&|!?:]"),
        BINARY_OPERATION("[-+*/]"),
        LEFT_PARENTHESIS("\\("),
        RIGHT_PARENTHESIS("\\)"),
        LEFT_BRACE("\\{"),
        RIGHT_BRACE("\\}"),
        DOT("\\."),
        COMMA("\\,"),
        EQUAL("="),
        SEMICOLON(";");

        private final Pattern pattern;

        TokenType(String regex) {
            this.pattern = Pattern.compile(regex);
        }

        public boolean validVariableName() {
            return this == NUMERAL || this == ALPHANUMERIC || this == DOT;
        }

        public boolean canNegate() {
            return this == NUMERAL || this == ALPHANUMERIC || this == THIS;
        }

        public boolean isTerminating() {
            return this == SEMICOLON;
        }

        public boolean isOutOfScope() {
            return this == RIGHT_PARENTHESIS || this == RIGHT_BRACE || this == COMMA;
        }
    }
}
