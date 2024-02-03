package gg.moonflower.molangcompiler.core.ast;

import gg.moonflower.molangcompiler.api.exception.MolangException;
import gg.moonflower.molangcompiler.api.exception.MolangSyntaxException;
import gg.moonflower.molangcompiler.core.compiler.BytecodeCompiler;
import gg.moonflower.molangcompiler.core.compiler.MolangBytecodeEnvironment;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;

/**
 * Compares the two values and runs an operation on them.
 *
 * @param operator The operator to apply
 * @param left     The first operand
 * @param right    The second operand
 * @author Buddy
 */
@ApiStatus.Internal
public record BinaryOperationNode(BinaryOperation operator, Node left, Node right) implements Node {

    @Override
    public String toString() {
        return "(" + this.left + " " + this.operator + " " + this.right + ")";
    }

    @Override
    public boolean isConstant() {
        return this.left.isConstant() && (this.operator == BinaryOperation.NULL_COALESCING || this.right.isConstant());
    }

    @Override
    public boolean hasValue() {
        return true;
    }

    @Override
    public float evaluate(MolangBytecodeEnvironment environment) throws MolangException {
        float left = this.left.evaluate(environment);
        float right = this.right.evaluate(environment);
        return switch (this.operator) {
            case ADD -> left + right;
            case SUBTRACT -> left - right;
            case MULTIPLY -> left * right;
            case DIVIDE -> left / right;
            case AND -> left != 0 && right != 0 ? 1.0F : 0.0F;
            case OR -> left != 0 || right != 0 ? 1.0F : 0.0F;
            case LESS -> left < right ? 1.0F : 0.0F;
            case LESS_EQUALS -> left <= right ? 1.0F : 0.0F;
            case GREATER -> left > right ? 1.0F : 0.0F;
            case GREATER_EQUALS -> left >= right ? 1.0F : 0.0F;
            case EQUALS -> left == right ? 1.0F : 0.0F;
            case NOT_EQUALS -> left != right ? 1.0F : 0.0F;
            // If the left is constant, then the value always exists and returns the first value
            case NULL_COALESCING -> left;
        };
    }

    @Override
    public void writeBytecode(MethodNode method, MolangBytecodeEnvironment environment, @Nullable Label breakLabel, @Nullable Label continueLabel) throws MolangException {
        if (environment.optimize()) {
            if (this.isConstant()) {
                BytecodeCompiler.writeFloatConst(method, this.evaluate(environment));
                return;
            }
        }

        switch (this.operator) {
            case AND -> {
                Label label_false = new Label();
                Label label_end = new Label();
                writeNode(this.left, method, environment, breakLabel, continueLabel);
                //left == 0: goto false
                method.visitInsn(Opcodes.FCONST_0);
                method.visitInsn(Opcodes.FCMPL);
                method.visitJumpInsn(Opcodes.IFEQ, label_false);

                //right == 0: goto false
                writeNode(this.right, method, environment, breakLabel, continueLabel);
                method.visitInsn(Opcodes.FCONST_0);
                method.visitInsn(Opcodes.FCMPL);
                method.visitJumpInsn(Opcodes.IFEQ, label_false);

                //else: true
                method.visitInsn(Opcodes.FCONST_1);
                method.visitJumpInsn(Opcodes.GOTO, label_end);

                //false:
                method.visitLabel(label_false);
                method.visitInsn(Opcodes.FCONST_0);

                //end:
                method.visitLabel(label_end);
            }
            case OR -> {
                Label label_true = new Label();
                Label label_end = new Label();
                //left != 0: goto true
                writeNode(this.left, method, environment, breakLabel, continueLabel);
                method.visitInsn(Opcodes.FCONST_0);
                method.visitInsn(Opcodes.FCMPL);
                method.visitJumpInsn(Opcodes.IFNE, label_true);

                //right != 0: goto true
                writeNode(this.right, method, environment, breakLabel, continueLabel);
                method.visitInsn(Opcodes.FCONST_0);
                method.visitInsn(Opcodes.FCMPL);
                method.visitJumpInsn(Opcodes.IFNE, label_true);

                //else: false
                method.visitInsn(Opcodes.FCONST_0);
                method.visitJumpInsn(Opcodes.GOTO, label_end);

                //true:
                method.visitLabel(label_true);
                method.visitInsn(Opcodes.FCONST_1);

                //end:
                method.visitLabel(label_end);
            }
            case NULL_COALESCING -> {
                if (!(this.left instanceof VariableGetNode lookup)) {
                    throw new MolangSyntaxException("Expected variable lookup, got " + this.left);
                }

                // Test if variable exists
                environment.loadObjectHas(method, lookup.object(), lookup.name());

                // Run branches
                Label label_false = new Label();
                Label label_end = new Label();
                method.visitJumpInsn(Opcodes.IFEQ, label_false);
                writeNode(this.left, method, environment, breakLabel, continueLabel);
                method.visitJumpInsn(Opcodes.GOTO, label_end);
                method.visitLabel(label_false);
                writeNode(this.right, method, environment, breakLabel, continueLabel);
                method.visitLabel(label_end);
            }
            case MULTIPLY -> {
                if (environment.optimize() && this.tryWriteNegate(method, environment, breakLabel, continueLabel)) {
                    return;
                }

                writeNode(this.left, method, environment, breakLabel, continueLabel);
                writeNode(this.right, method, environment, breakLabel, continueLabel);
                method.visitInsn(Opcodes.FMUL);
            }
            case DIVIDE -> {
                if (environment.optimize() && this.tryWriteNegate(method, environment, breakLabel, continueLabel)) {
                    return;
                }

                writeNode(this.left, method, environment, breakLabel, continueLabel);
                writeNode(this.right, method, environment, breakLabel, continueLabel);
                method.visitInsn(Opcodes.FDIV);
            }
            default -> {
                writeNode(this.left, method, environment, breakLabel, continueLabel);
                writeNode(this.right, method, environment, breakLabel, continueLabel);

                switch (this.operator) {
                    case ADD -> method.visitInsn(Opcodes.FADD);
                    case SUBTRACT -> method.visitInsn(Opcodes.FSUB);
                    case EQUALS -> writeComparision(method, Opcodes.IFNE);
                    case NOT_EQUALS -> writeComparision(method, Opcodes.IFEQ);
                    case LESS_EQUALS -> writeComparision(method, Opcodes.IFGT);
                    case LESS -> writeComparision(method, Opcodes.IFGE);
                    case GREATER_EQUALS -> writeComparision(method, Opcodes.IFLT);
                    case GREATER -> writeComparision(method, Opcodes.IFLE);
                }
            }
        }
    }

    // Try to replace with the negate operation if multiplying/dividing by -1
    private boolean tryWriteNegate(MethodNode method, MolangBytecodeEnvironment environment, @Nullable Label breakLabel, @Nullable Label continueLabel) throws MolangException {
        if (this.left.isConstant()) {
            float left = this.left.evaluate(environment);
            if (left == -1.0F) {
                this.right.writeBytecode(method, environment, breakLabel, continueLabel);
                method.visitInsn(Opcodes.FNEG);
                return true;
            }
        } else if (this.right.isConstant()) {
            float right = this.right.evaluate(environment);
            if (right == -1.0F) {
                this.left.writeBytecode(method, environment, breakLabel, continueLabel);
                method.visitInsn(Opcodes.FNEG);
                return true;
            }
        }
        return false;
    }

    private static void writeNode(Node node, MethodNode method, MolangBytecodeEnvironment environment, @Nullable Label breakLabel, @Nullable Label continueLabel) throws MolangException {
        if (environment.optimize() && node.isConstant()) {
            BytecodeCompiler.writeFloatConst(method, node.evaluate(environment));
        } else {
            node.writeBytecode(method, environment, breakLabel, continueLabel);
        }
    }

    private static void writeComparision(MethodNode method, int success) {
        Label label_false = new Label();
        Label label_end = new Label();
        method.visitInsn(Opcodes.FCMPL);
        method.visitJumpInsn(success, label_false);
        method.visitInsn(Opcodes.FCONST_1);
        method.visitJumpInsn(Opcodes.GOTO, label_end);
        method.visitLabel(label_false);
        method.visitInsn(Opcodes.FCONST_0);
        method.visitLabel(label_end);
    }
}
