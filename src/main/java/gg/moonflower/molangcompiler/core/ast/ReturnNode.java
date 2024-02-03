package gg.moonflower.molangcompiler.core.ast;

import gg.moonflower.molangcompiler.api.exception.MolangException;
import gg.moonflower.molangcompiler.core.compiler.BytecodeCompiler;
import gg.moonflower.molangcompiler.core.compiler.MolangBytecodeEnvironment;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;

/**
 * Returns the value on the stack from the specified value.
 *
 * @param value The value to return
 * @author Buddy
 */
@ApiStatus.Internal
public record ReturnNode(Node value) implements Node {

    @Override
    public String toString() {
        return "return " + this.value.toString();
    }

    @Override
    public boolean isConstant() {
        return this.value.isConstant();
    }

    @Override
    public boolean hasValue() {
        return false;
    }

    @Override
    public float evaluate(MolangBytecodeEnvironment environment) throws MolangException {
        return this.value.evaluate(environment);
    }

    @Override
    public void writeBytecode(MethodNode method, MolangBytecodeEnvironment environment, @Nullable Label breakLabel, @Nullable Label continueLabel) throws MolangException {
        if (environment.optimize() && this.isConstant()) {
            BytecodeCompiler.writeFloatConst(method, this.evaluate(environment));
        } else {
            this.value.writeBytecode(method, environment, breakLabel, continueLabel);
            if (!this.value.hasValue()) {
                method.visitInsn(Opcodes.FCONST_0);
            }
        }
        environment.writeModifiedVariables(method);
        method.visitInsn(Opcodes.FRETURN);
    }
}