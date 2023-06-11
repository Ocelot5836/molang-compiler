package io.github.ocelot.molangcompiler.core.ast;

import io.github.ocelot.molangcompiler.api.exception.MolangException;
import io.github.ocelot.molangcompiler.core.compiler.MolangBytecodeEnvironment;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;

/**
 * Sets the value of a variable.
 *
 * @param object      The object the variable is stored in
 * @param name        The name of the variable
 * @param value       The value to set to the variable
 * @param returnValue Whether to return the new value of the variable
 * @author Ocelot
 */
public record VariableSetNode(String object, String name, Node value, boolean returnValue) implements OptionalValueNode {

    public VariableSetNode(String object, String name, Node value) {
        this(object, name, value, false);
    }

    @Override
    public String toString() {
        return "variable." + this.name + " = " + this.value;
    }

    @Override
    public boolean isConstant() {
        return false;
    }

    @Override
    public boolean hasValue() {
        return this.returnValue;
    }

    @Override
    public void writeBytecode(MethodNode method, MolangBytecodeEnvironment environment, @Nullable Label breakLabel, @Nullable Label continueLabel) throws MolangException {
        int index = environment.loadVariable(method, this.object, this.name);
        this.value.writeBytecode(method, environment, breakLabel, continueLabel);
        method.visitVarInsn(Opcodes.FSTORE, index);
        if (this.returnValue) {
            method.visitVarInsn(Opcodes.FLOAD, index);
        }
        environment.markDirty(this.object, this.name);
    }

    @Override
    public VariableSetNode withReturnValue() {
        return this.returnValue ? this : new VariableSetNode(this.object, this.name, this.value, true);
    }
}