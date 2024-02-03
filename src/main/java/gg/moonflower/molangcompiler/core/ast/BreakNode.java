package gg.moonflower.molangcompiler.core.ast;

import gg.moonflower.molangcompiler.api.exception.MolangException;
import gg.moonflower.molangcompiler.api.exception.MolangSyntaxException;
import gg.moonflower.molangcompiler.core.compiler.MolangBytecodeEnvironment;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;

/**
 * Breaks free from a loop if currently looping.
 *
 * @author Buddy
 */
@ApiStatus.Internal
public record BreakNode() implements Node {

    @Override
    public String toString() {
        return "break";
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
        if (breakLabel == null) {
            throw new MolangSyntaxException("Cannot break outside of loop");
        }
        method.visitJumpInsn(Opcodes.GOTO, breakLabel);
    }
}
