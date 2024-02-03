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
 * Negates the specified boolean value.
 *
 * @param value The value to negate
 */
@ApiStatus.Internal
public record NegateNode(Node value) implements Node {

    @Override
    public String toString() {
        return "!" + this.value;
    }

    @Override
    public boolean isConstant() {
        return this.value.isConstant();
    }

    @Override
    public boolean hasValue() {
        return true;
    }

    @Override
    public float evaluate(MolangBytecodeEnvironment environment) throws MolangException {
        return this.value.evaluate(environment) == 0.0F ? 1.0F : 0.0F;
    }

    @Override
    public void writeBytecode(MethodNode method, MolangBytecodeEnvironment environment, @Nullable Label breakLabel, @Nullable Label continueLabel) throws MolangException {
        if (environment.optimize() && this.isConstant()) {
            BytecodeCompiler.writeFloatConst(method, this.evaluate(environment));
            return;
        }

        Label label_right = new Label();
        Label label_end = new Label();

        this.value.writeBytecode(method, environment, breakLabel, continueLabel);
        method.visitInsn(Opcodes.FCONST_0);
        method.visitInsn(Opcodes.FCMPL);

        //value ?
        method.visitJumpInsn(Opcodes.IFEQ, label_right);

        // 1
        method.visitInsn(Opcodes.FCONST_1);
        method.visitJumpInsn(Opcodes.GOTO, label_end);

        //: 0
        method.visitLabel(label_right);
        method.visitInsn(Opcodes.FCONST_0);

        method.visitLabel(label_end);
    }
}
