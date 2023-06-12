package gg.moonflower.molangcompiler.core.compiler;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class TokenReader extends StringReader {

    private final MolangTokenizer.Token[] tokens;

    public TokenReader(MolangTokenizer.Token[] tokens) {
        super("");
        this.tokens = tokens;
    }

    @Override
    public String getString() {
        StringBuilder builder = new StringBuilder();
        for (MolangTokenizer.Token token : this.tokens) {
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

    public MolangTokenizer.Token peekAfter(int amount) {
        return this.tokens[this.cursor + amount];
    }

    public MolangTokenizer.Token peek() {
        return this.tokens[this.cursor];
    }
}
