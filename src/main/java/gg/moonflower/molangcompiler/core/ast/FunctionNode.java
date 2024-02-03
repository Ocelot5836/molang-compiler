package gg.moonflower.molangcompiler.core.ast;

import gg.moonflower.molangcompiler.api.exception.MolangException;
import gg.moonflower.molangcompiler.core.compiler.BytecodeCompiler;
import gg.moonflower.molangcompiler.core.compiler.MolangBytecodeEnvironment;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Invokes a function from an object.
 *
 * @param object    The object to run the function from
 * @param function  The name of the function to run
 * @param arguments The parameters to pass into the function
 * @author Ocelot
 */
@ApiStatus.Internal
public record FunctionNode(String object, String function, Node... arguments) implements Node {

    @Override
    public String toString() {
        return this.object + "." + this.function + "(" + Arrays.stream(this.arguments).map(Node::toString).collect(Collectors.joining(", ")) + ")";
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
        int objectIndex = environment.getObjectIndex(method, this.object);

        // Function
        method.visitVarInsn(Opcodes.ALOAD, objectIndex);
        method.visitLdcInsn(this.function);
        method.visitLdcInsn(this.function + "$" + this.arguments.length);
        method.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                "gg/moonflower/molangcompiler/core/MolangUtil",
                "getFunction",
                "(Lgg/moonflower/molangcompiler/api/object/MolangObject;Ljava/lang/String;Ljava/lang/String;)Lgg/moonflower/molangcompiler/api/MolangExpression;",
                false
        );
        int expressionIndex = environment.allocateVariable(this.object + "." + this.function + "$" + this.arguments.length);
        method.visitVarInsn(Opcodes.ASTORE, expressionIndex);

        // Parameters
        for (Node node : this.arguments) {
            boolean full = !environment.optimize() || !node.isConstant();
            if (full) {
                node.writeBytecode(method, environment, breakLabel, continueLabel);
            }

            method.visitVarInsn(Opcodes.ALOAD, BytecodeCompiler.RUNTIME_INDEX);
            if (full) {
                method.visitInsn(Opcodes.SWAP);
            } else {
                BytecodeCompiler.writeFloatConst(method, node.evaluate(environment));
            }
            method.visitMethodInsn(
                    Opcodes.INVOKEINTERFACE,
                    "gg/moonflower/molangcompiler/api/MolangEnvironment",
                    "loadParameter",
                    "(F)V",
                    true
            );
        }

        // Resolve Function
        method.visitVarInsn(Opcodes.ALOAD, expressionIndex);
        method.visitVarInsn(Opcodes.ALOAD, BytecodeCompiler.RUNTIME_INDEX);
        method.visitMethodInsn(
                Opcodes.INVOKEINTERFACE,
                "gg/moonflower/molangcompiler/api/MolangExpression",
                "resolve",
                "(Lgg/moonflower/molangcompiler/api/MolangEnvironment;)F",
                true
        );

        // Clear parameters
        method.visitVarInsn(Opcodes.ALOAD, BytecodeCompiler.RUNTIME_INDEX);
        method.visitMethodInsn(
                Opcodes.INVOKEINTERFACE,
                "gg/moonflower/molangcompiler/api/MolangEnvironment",
                "clearParameters",
                "()V",
                true
        );
    }
}
