package gg.moonflower.molangcompiler.impl.ast;

import gg.moonflower.molangcompiler.api.exception.MolangException;
import gg.moonflower.molangcompiler.impl.compiler.MolangBytecodeEnvironment;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Label;
import org.objectweb.asm.tree.MethodNode;

/**
 * @author Buddy
 */
@ApiStatus.Internal
public interface Node {

    @Override
    String toString();

    /**
     * @return Whether this node can be evaluated without a runtime environment
     */
    boolean isConstant();

    /**
     * @return Whether this node pushes a value onto the stack that can be returned
     */
    boolean hasValue();

    /**
     * Attempts to statically evaluate this expression. This will fail if {@link #isConstant()} is <code>false</code>.
     *
     * @return The static float value of this node
     * @throws MolangException If any error occurred while trying to evaluate this node
     */
    default float evaluate(MolangBytecodeEnvironment environment) throws MolangException {
        throw new MolangException("Cannot statically evaluate " + this.getClass().getSimpleName());
    }

    /**
     * Writes java bytecode representing this node to the specified method.
     *
     * @param method        The method to write into
     * @param environment   The bytecode compilation environment
     * @param breakLabel    A label to break out of loops or <code>null</code> if not in a loop
     * @param continueLabel A label to continue to the next loop iteration or <code>null</code> if not in a loop
     * @throws MolangException If any syntax problems prevent the expression from being written
     */
    default void writeBytecode(MethodNode method, MolangBytecodeEnvironment environment, @Nullable Label breakLabel, @Nullable Label continueLabel) throws MolangException {
        throw new MolangException("Not implemented (" + this.getClass().getSimpleName() + " " + this + ")");
    }
}