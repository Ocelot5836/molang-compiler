package gg.moonflower.molangcompiler.core.ast;

import gg.moonflower.molangcompiler.api.exception.MolangException;
import gg.moonflower.molangcompiler.core.compiler.MolangBytecodeEnvironment;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;

/**
 * Performs an "if" check on the specified value and executes the branch if it passes.
 *
 * @param value  The value to check. If not zero it is considered <code>true</code>
 * @param branch The value to use when the check passes
 * @author Buddy
 */
@ApiStatus.Internal
public record BinaryConditionalNode(Node value, Node branch) implements Node {

    @Override
    public String toString() {
        return this.value + " ? " + this.branch;
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
        return this.value.evaluate(environment) != 0.0F ? this.branch.evaluate(environment) : 0.0F;
    }

    @Override
    public void writeBytecode(MethodNode method, MolangBytecodeEnvironment environment, @Nullable Label breakLabel, @Nullable Label continueLabel) throws MolangException {
        Label label_end = new Label();

        if (environment.optimize() && this.value.isConstant()) {
            if (this.value.evaluate(environment) != 0.0F) {
                this.branch.writeBytecode(method, environment, breakLabel, continueLabel);
            }
            return;
        }

        this.value.writeBytecode(method, environment, breakLabel, continueLabel);
        method.visitInsn(Opcodes.FCONST_0);
        method.visitInsn(Opcodes.FCMPL);
        method.visitJumpInsn(Opcodes.IFEQ, label_end);
        this.branch.writeBytecode(method, environment, breakLabel, continueLabel);
        method.visitLabel(label_end);
    }
}
