// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.

package gg.moonflower.molangcompiler.api.exception;

import org.jetbrains.annotations.Nullable;


/**
 * Thrown when any exception occurs when parsing a MoLang expression.
 *
 * <p> Modified version of CommandSyntaxException from <a href=https://github.com/Mojang/brigadier/blob/master/src/main/java/com/mojang/brigadier/exceptions/CommandSyntaxException.java>Brigadier</a>
 *
 * @since 1.0.0
 */
public class MolangSyntaxException extends MolangException {

    public static final int CONTEXT_AMOUNT = 64;

    private final String message;
    private final String input;
    private final int cursor;

    public MolangSyntaxException(String message) {
        super(message);
        this.message = message;
        this.input = null;
        this.cursor = -1;
    }

    public MolangSyntaxException(String message, Throwable t) {
        super(message, t);
        this.message = message;
        this.input = null;
        this.cursor = -1;
    }

    public MolangSyntaxException(String message, String input) {
        this(message, input, input.length());
    }

    public MolangSyntaxException(String message, String input, int cursor) {
        super(message);
        this.message = message;
        this.input = input;
        this.cursor = cursor;
    }

    @Override
    public String getMessage() {
        String message = this.message;
        String context = this.getContext();
        if (context != null) {
            message += " at position " + this.cursor + ": " + context;
        }
        return message;
    }

    /**
     * @return The raw message error
     */
    public String getRawMessage() {
        return message;
    }

    /**
     * @return The additional context if there is any
     */
    public String getContext() {
        if (this.input == null || this.cursor < 0) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        int cursor = Math.min(this.input.length(), this.cursor);

        if (cursor > CONTEXT_AMOUNT) {
            builder.append("...");
        }

        builder.append(this.input, Math.max(0, cursor - CONTEXT_AMOUNT), cursor);
        builder.append("<--[HERE]");

        return builder.toString();
    }

    /**
     * @return The raw input data or <code>null</code> if no context was specified
     */
    @Nullable
    public String getInput() {
        return input;
    }

    /**
     * @return The cursor position when the error occurs or <code>-1</code> if no context was specified
     */
    public int getCursor() {
        return cursor;
    }
}