package gg.moonflower.molangcompiler.impl.compiler;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class TokenReader extends StringReader {

    private final MolangLexer.Token[] tokens;

    public TokenReader(MolangLexer.Token[] tokens) {
        super("");
        this.tokens = tokens;
    }

    @Override
    public String getString() {
        StringBuilder builder = new StringBuilder();
        for (MolangLexer.Token token : this.tokens) {
            builder.append(token.value());
        }
        return builder.toString();
    }

    @Override
    public boolean canRead(int length) {
        return this.cursor + length <= this.tokens.length;
    }

    public int getCursorOffset() {
        int offset = 0;
        for (int i = 0; i <= Math.min(this.cursor, this.tokens.length - 1); i++) {
            offset += this.tokens[i].value().length();
        }
        return offset;
    }

    public MolangLexer.Token peekAfter(int amount) {
        return this.tokens[this.cursor + amount];
    }

    public MolangLexer.Token peek() {
        return this.tokens[this.cursor];
    }
}
