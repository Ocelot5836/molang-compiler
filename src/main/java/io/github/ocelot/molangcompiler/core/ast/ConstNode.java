package io.github.ocelot.molangcompiler.core.ast;

import io.github.ocelot.molangcompiler.api.exception.MolangException;
import io.github.ocelot.molangcompiler.core.compiler.BytecodeCompiler;
import io.github.ocelot.molangcompiler.core.compiler.MolangBytecodeEnvironment;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Label;
import org.objectweb.asm.tree.MethodNode;

/**
 * Pushes a single constant number onto the stack.
 *
 * @param value The value to push onto the stack
 * @author Buddy, Ocelot
 */
public record ConstNode(float value) implements Node {

    @Override
    public String toString() {
        return String.valueOf(this.value);
    }

    @Override
    public boolean isConstant() {
        return true;
    }

    @Override
    public float evaluate(MolangBytecodeEnvironment environment) throws MolangException {
        return this.value;
    }

    @Override
    public void writeBytecode(MethodNode method, MolangBytecodeEnvironment environment, @Nullable Label breakLabel, @Nullable Label continueLabel) throws MolangException {
        BytecodeCompiler.writeFloatConst(method, this.value);
    }
}