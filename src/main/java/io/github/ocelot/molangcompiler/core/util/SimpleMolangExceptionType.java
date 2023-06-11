// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.

package io.github.ocelot.molangcompiler.core.util;

import io.github.ocelot.molangcompiler.api.exception.MolangSyntaxException;
import org.jetbrains.annotations.ApiStatus;

/**
 * Modified version of SimpleCommandExceptionType from <a href=https://github.com/Mojang/brigadier/blob/master/src/main/java/com/mojang/brigadier/exceptions/SimpleCommandExceptionType.java>Brigadier</a>
 */
@ApiStatus.Internal
public class SimpleMolangExceptionType {

    private final String message;

    public SimpleMolangExceptionType(String message) {
        this.message = message;
    }

    public MolangSyntaxException create() {
        return new MolangSyntaxException(this.message);
    }

    public MolangSyntaxException createWithContext(StringReader reader) {
        return new MolangSyntaxException(this.message, reader.getString(), reader.getCursor());
    }

    @Override
    public String toString() {
        return message;
    }
}