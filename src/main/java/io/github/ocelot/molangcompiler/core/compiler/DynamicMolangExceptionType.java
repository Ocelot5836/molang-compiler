// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.

package io.github.ocelot.molangcompiler.core.compiler;

import io.github.ocelot.molangcompiler.api.exception.MolangSyntaxException;

import java.util.function.Function;

/**
 * <p>Modified version of DynamicCommandExceptionType from <a href=https://github.com/Mojang/brigadier/blob/master/src/main/java/com/mojang/brigadier/exceptions/DynamicCommandExceptionType.java>Brigadier</a></p>
 */
public class DynamicMolangExceptionType
{
    private final Function<Object, String> function;

    public DynamicMolangExceptionType(Function<Object, String> function)
    {
        this.function = function;
    }

    public MolangSyntaxException create(Object arg)
    {
        return new MolangSyntaxException(this.function.apply(arg));
    }

    public MolangSyntaxException createWithContext(StringReader reader, Object arg)
    {
        return new MolangSyntaxException(this.function.apply(arg), reader.getString(), reader.getCursor());
    }
}