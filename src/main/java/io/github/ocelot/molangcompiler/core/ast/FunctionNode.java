package io.github.ocelot.molangcompiler.core.ast;

import io.github.ocelot.molangcompiler.api.exception.MolangException;
import io.github.ocelot.molangcompiler.core.compiler.BytecodeCompiler;
import io.github.ocelot.molangcompiler.core.compiler.MolangBytecodeEnvironment;
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
    public void writeBytecode(MethodNode method, MolangBytecodeEnvironment environment, @Nullable Label breakLabel, @Nullable Label continueLabel) throws MolangException {
        int objectIndex = environment.getObjectIndex(method, this.object);

        // Function
        method.visitVarInsn(Opcodes.ALOAD, objectIndex);
        method.visitLdcInsn(this.function);
        method.visitLdcInsn(this.function + "$" + this.arguments.length);
        method.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                "io/github/ocelot/molangcompiler/core/MolangUtil",
                "getFunction",
                "(Lio/github/ocelot/molangcompiler/api/object/MolangObject;Ljava/lang/String;Ljava/lang/String;)Lio/github/ocelot/molangcompiler/api/MolangExpression;",
                false
        );
        int expressionIndex = environment.allocateVariable(this.object + "." + this.function + "$" + this.arguments.length);
        method.visitVarInsn(Opcodes.ASTORE, expressionIndex);

        // Parameters
        int temp = environment.getTempVariableIndex(0);
        for (Node node : this.arguments) {
            boolean full = !environment.optimize() || !node.isConstant();
            if (full) {
                node.writeBytecode(method, environment, breakLabel, continueLabel);
                method.visitVarInsn(Opcodes.FSTORE, temp);
            }

            method.visitVarInsn(Opcodes.ALOAD, BytecodeCompiler.RUNTIME_INDEX);
            if (full) {
                method.visitVarInsn(Opcodes.FLOAD, temp);
            } else {
                BytecodeCompiler.writeFloatConst(method, node.evaluate(environment));
            }
            method.visitMethodInsn(
                    Opcodes.INVOKEINTERFACE,
                    "io/github/ocelot/molangcompiler/api/MolangEnvironment",
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
                "io/github/ocelot/molangcompiler/api/MolangExpression",
                "resolve",
                "(Lio/github/ocelot/molangcompiler/api/MolangEnvironment;)F",
                true
        );
        // Store result
        method.visitVarInsn(Opcodes.FSTORE, temp);

        // Clear parameters
        method.visitVarInsn(Opcodes.ALOAD, BytecodeCompiler.RUNTIME_INDEX);
        method.visitMethodInsn(
                Opcodes.INVOKEINTERFACE,
                "io/github/ocelot/molangcompiler/api/MolangEnvironment",
                "clearParameters",
                "()V",
                true
        );

        // Load result
        method.visitVarInsn(Opcodes.FLOAD, temp);
    }
}
