package io.github.ocelot.molangcompiler.core.compiler;

import io.github.ocelot.molangcompiler.api.exception.MolangSyntaxException;
import io.github.ocelot.molangcompiler.core.util.SimpleMolangExceptionType;
import io.github.ocelot.molangcompiler.core.util.StringReader;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ApiStatus.Internal
public final class MolangTokenizer {

    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s");

    public static Token[] createTokens(String input) throws MolangSyntaxException {
        StringReader reader = new StringReader(WHITESPACE_PATTERN.matcher(input).replaceAll(""));
        List<Token> tokens = new ArrayList<>();

        while (reader.canRead()) {
            Token token = getToken(reader);
            if (token != null) {
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
    }

    public enum TokenType {
        NUMERAL("-?\\d+"),
        ALPHANUMERIC("[A-Za-z_][A-Za-z0-9_]*"),
        NULL_COALESCING("\\?\\?"),
        SPECIAL("[-+*/<>&|!?:]+"),
        LEFT_PARENTHESIS("\\("),
        RIGHT_PARENTHESIS("\\)"),
//        LEFT_BRACKET("\\["),
//        RIGHT_BRACKET("\\]"),
        LEFT_BRACE("\\{"),
        RIGHT_BRACE("\\}"),
        DOT("\\."),
        COMMA("\\,"),
        EQUAL("="),
        SEMICOLON(";");
//        ARROW("->");

        private final Pattern pattern;

        TokenType(String regex) {
            this.pattern = Pattern.compile(regex);
        }
    }
}
