package io.github.ocelot.molangcompiler.core.ast;

import io.github.ocelot.molangcompiler.api.exception.MolangException;
import io.github.ocelot.molangcompiler.api.exception.MolangSyntaxException;
import io.github.ocelot.molangcompiler.core.compiler.MolangBytecodeEnvironment;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;

/**
 * Breaks free from a loop if currently looping.
 *
 * @author Buddy
 */
public record BreakNode() implements Node {

    @Override
    public boolean isConstant() {
        return false;
    }

    @Override
    public void writeBytecode(MethodNode method, MolangBytecodeEnvironment environment, @Nullable Label breakLabel, @Nullable Label continueLabel) throws MolangException {
        if (breakLabel == null) {
            throw new MolangSyntaxException("Cannot break outside of loop");
        }
        method.visitJumpInsn(Opcodes.GOTO, breakLabel);
    }
}
