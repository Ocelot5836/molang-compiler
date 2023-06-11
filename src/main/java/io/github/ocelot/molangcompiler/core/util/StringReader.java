// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.

package io.github.ocelot.molangcompiler.core.util;

import org.jetbrains.annotations.ApiStatus;

/**
 * Modified version of StringReader from <a href=https://github.com/Mojang/brigadier/blob/master/src/main/java/com/mojang/brigadier/StringReader.java>Brigadier</a>
 */
@ApiStatus.Internal
public class StringReader {

    private final String string;
    private int cursor;

    public StringReader(String string) {
        this.string = string;
    }

    public String getString() {
        return string;
    }

    public void setCursor(final int cursor) {
        this.cursor = cursor;
    }

    public int getCursor() {
        return cursor;
    }

    public String getRead() {
        return this.string.substring(0, this.cursor);
    }

    public String getRemaining() {
        return this.string.substring(this.cursor);
    }

    public boolean canRead(final int length) {
        return this.cursor + length <= this.string.length();
    }

    public boolean canRead() {
        return canRead(1);
    }

    public char peekBefore(int i) {
        return this.string.charAt(this.cursor - i);
    }

    public char peek() {
        return this.string.charAt(this.cursor);
    }

    public void skip() {
        this.cursor++;
    }

    public void skip(int amount) {
        this.cursor += amount;
    }

    public void skipWhitespace() {
        while (this.canRead() && Character.isWhitespace(this.peek())) {
            this.skip();
        }
    }
}
