// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.

package io.github.ocelot.molangcompiler.core.util;

import io.github.ocelot.molangcompiler.api.exception.MolangSyntaxException;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.BiFunction;

/**
 * Modified version of Dynamic2CommandExceptionType from <a href=https://github.com/Mojang/brigadier/blob/master/src/main/java/com/mojang/brigadier/exceptions/Dynamic2CommandExceptionType.java>Brigadier</a>
 */
@ApiStatus.Internal
public class Dynamic2MolangExceptionType {

    private final BiFunction<Object, Object, String> function;

    public Dynamic2MolangExceptionType(BiFunction<Object, Object, String> function) {
        this.function = function;
    }

    public MolangSyntaxException create(Object a, Object b) {
        return new MolangSyntaxException(this.function.apply(a, b));
    }

    public MolangSyntaxException createWithContext(StringReader reader, Object a, Object b) {
        return new MolangSyntaxException(this.function.apply(a, b), reader.getString(), reader.getCursor());
    }
}