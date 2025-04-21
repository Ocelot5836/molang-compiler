// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.

package gg.moonflower.molangcompiler.impl.compiler;

import org.jetbrains.annotations.ApiStatus;

/**
 * Modified version of StringReader from <a href=https://github.com/Mojang/brigadier/blob/master/src/main/java/com/mojang/brigadier/StringReader.java>Brigadier</a>
 */
@ApiStatus.Internal
public class StringReader {

    private final String string;
    protected int cursor;

    public StringReader(String string) {
        this.string = string;
    }

    public String getString() {
        return this.string;
    }

    public int getCursor() {
        return this.cursor;
    }

    public void setCursor(int cursor) {
        this.cursor = cursor;
    }

    public boolean canRead(int length) {
        return this.cursor + length <= this.string.length();
    }

    public boolean canRead() {
        return this.canRead(1);
    }

    public void skip(int amount) {
        this.cursor += amount;
    }

    public void skip() {
        this.cursor++;
    }

    public void skipWhitespace() {
        while (this.canRead() && Character.isWhitespace(this.string.charAt(this.cursor))) {
            this.skip();
        }
    }
}
