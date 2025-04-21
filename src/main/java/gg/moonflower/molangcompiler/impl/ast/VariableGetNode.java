package gg.moonflower.molangcompiler.impl.ast;

import gg.moonflower.molangcompiler.api.exception.MolangException;
import gg.moonflower.molangcompiler.impl.compiler.MolangBytecodeEnvironment;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;

/**
 * Retrieves the value of a variable and puts it onto the stack.
 *
 * @param object The object the variable is stored in
 * @param name   The name of the variable
 * @author Ocelot
 */
@ApiStatus.Internal
public record VariableGetNode(String object, String name) implements Node {

    @Override
    public String toString() {
        return this.object + "." + this.name;
    }

    @Override
    public boolean isConstant() {
        return false;
    }

    @Override
    public boolean hasValue() {
        return true;
    }

    @Override
    public void writeBytecode(MethodNode method, MolangBytecodeEnvironment environment, @Nullable Label breakLabel, @Nullable Label continueLabel) throws MolangException {
        int index = environment.loadVariable(method, this.object, this.name);
        method.visitVarInsn(Opcodes.FLOAD, index);
    }
}