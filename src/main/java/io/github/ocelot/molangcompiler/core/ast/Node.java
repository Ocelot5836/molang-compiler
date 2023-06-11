package io.github.ocelot.molangcompiler.core.ast;

import io.github.ocelot.molangcompiler.api.exception.MolangException;
import io.github.ocelot.molangcompiler.core.compiler.MolangBytecodeEnvironment;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Label;
import org.objectweb.asm.tree.MethodNode;

/**
 * @author Buddy
 */
public interface Node {

    @Override
    String toString();

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

    default void writeBytecode(MethodNode method, MolangBytecodeEnvironment environment, @Nullable Label breakLabel, @Nullable Label continueLabel) throws MolangException {
        throw new MolangException("Not implemented (" + this.getClass().getSimpleName() + " " + this + ")");
    }
}