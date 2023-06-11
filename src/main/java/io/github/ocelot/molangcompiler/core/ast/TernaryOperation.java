package io.github.ocelot.molangcompiler.core.ast;

import io.github.ocelot.molangcompiler.api.exception.MolangException;
import io.github.ocelot.molangcompiler.core.compiler.MolangBytecodeEnvironment;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;

/**
 * Performs an "if" check on the specified value and chooses a branch.
 *
 * @param value The value to check. If not zero it is considered <code>true</code>
 * @param left  The value to use when true
 * @param right The value to use when false
 * @author Buddy
 */
public record TernaryOperation(Node value, Node left, Node right) implements Node {

    @Override
    public String toString() {
        return this.value + " ? " + this.left + " : " + this.right;
    }

    @Override
    public boolean isConstant() {
        return this.value.isConstant();
    }

    @Override
    public float evaluate(MolangBytecodeEnvironment environment) throws MolangException {
        return this.value.evaluate(environment) != 0.0F ? this.left.evaluate(environment) : this.right.evaluate(environment);
    }

    @Override
    public void writeBytecode(MethodNode method, MolangBytecodeEnvironment environment, @Nullable Label breakLabel, @Nullable Label continueLabel) throws MolangException {
        Label label_right = new Label();
        Label label_end = new Label();

        if (environment.optimize() && this.value.isConstant()) {
            if (this.value.evaluate(environment) != 0.0F) {
                this.left.writeBytecode(method, environment, breakLabel, continueLabel);
            } else {
                this.right.writeBytecode(method, environment, breakLabel, continueLabel);
            }
            return;
        }

        this.value.writeBytecode(method, environment, breakLabel, continueLabel);
        method.visitInsn(Opcodes.FCONST_0);
        method.visitInsn(Opcodes.FCMPL);

        //value ?
        method.visitJumpInsn(Opcodes.IFEQ, label_right);

        //[left]
        this.left.writeBytecode(method, environment, breakLabel, continueLabel);
        method.visitJumpInsn(Opcodes.GOTO, label_end);

        //: [right]
        method.visitLabel(label_right);
        this.right.writeBytecode(method, environment, breakLabel, continueLabel);

        method.visitLabel(label_end);
    }
}
