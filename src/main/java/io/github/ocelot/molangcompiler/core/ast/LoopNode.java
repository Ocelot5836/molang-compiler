package io.github.ocelot.molangcompiler.core.ast;

import io.github.ocelot.molangcompiler.api.exception.MolangException;
import io.github.ocelot.molangcompiler.core.compiler.BytecodeCompiler;
import io.github.ocelot.molangcompiler.core.compiler.MolangBytecodeEnvironment;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;

/**
 * Runs the body based on the number of iterations requested.
 *
 * @param iterations The number of iterations to loop
 * @param body       The body of the loop
 * @author Buddy
 */
public record LoopNode(Node iterations, Node body) implements Node {

    @Override
    public String toString() {
        return "loop(" + this.iterations + ", {" + this.body + "})";
    }

    @Override
    public boolean isConstant() {
        return false;
    }

    @Override
    public boolean hasValue() {
        return false;
    }

    @Override
    public void writeBytecode(MethodNode method, MolangBytecodeEnvironment environment, @Nullable Label breakLabel, @Nullable Label continueLabel) throws MolangException {
        Label begin = new Label();
        Label end = new Label();

        // iterations
        this.iterations.writeBytecode(method, environment, breakLabel, continueLabel);
        method.visitInsn(Opcodes.F2I);

        BytecodeCompiler.writeIntConst(method, 0); // int i = 0;
        method.visitLabel(begin);

        this.body.writeBytecode(method, environment, end, begin);
        if (this.body.hasValue()) { // Must return void
            method.visitInsn(Opcodes.POP);
        }

        method.visitInsn(Opcodes.ICONST_1);
        method.visitInsn(Opcodes.IADD); // i++
        method.visitInsn(Opcodes.DUP2);
        method.visitJumpInsn(Opcodes.IF_ICMPGT, begin);
        method.visitLabel(end);
    }
}
