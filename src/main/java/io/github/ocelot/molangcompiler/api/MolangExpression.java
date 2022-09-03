package io.github.ocelot.molangcompiler.api;

import io.github.ocelot.molangcompiler.api.exception.MolangException;
import io.github.ocelot.molangcompiler.core.node.MolangConstantNode;
import io.github.ocelot.molangcompiler.core.node.MolangStaticNode;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * A math expression that can be reduced using a {@link MolangEnvironment}.
 *
 * @author Ocelot
 * @see MolangEnvironment
 * @since 1.0.0
 */
public interface MolangExpression
{
    MolangExpression ZERO = of(0);

    /**
     * Resolves the float value of this runtime.
     *
     * @param environment The environment to execute in
     * @return The resulting value
     * @throws MolangException If any error occurs when resolving the value
     */
    float resolve(MolangEnvironment environment) throws MolangException;

    /**
     * Resolves the float value of this runtime. Catches any exception thrown and returns <code>0.0</code>.
     *
     * @param environment The environment to execute in
     * @return The resulting value
     */
    default float safeResolve(MolangEnvironment environment)
    {
        try
        {
            return this.resolve(environment);
        }
        catch (MolangException e)
        {
            e.printStackTrace();
            return 0.0F;
        }
    }

    /**
     * Creates a {@link MolangExpression} of the specified value.
     *
     * @param value The value to represent as an expression
     * @return A new expression with that value
     */
    static MolangExpression of(float value)
    {
        return new MolangConstantNode(value);
    }

    /**
     * Creates a {@link MolangExpression} of the specified value.
     *
     * @param value The value to represent as an expression
     * @return A new expression with that value
     */
    static MolangExpression of(boolean value)
    {
        return new MolangConstantNode(value ? 1.0F : 0.0F);
    }

    /**
     * Creates a {@link MolangExpression} of the specified value that will be computed after every call.
     *
     * @param value The value to represent as an expression
     * @return A new expression with that value
     */
    static MolangExpression of(Supplier<Float> value)
    {
        return new MolangStaticNode(value);
    }

    /**
     * Creates a {@link MolangExpression} of the specified value that will be computed after every call.
     *
     * @param value The value to represent as an expression
     * @return A new expression with that value
     */
    static MolangExpression of(BooleanSupplier value)
    {
        return new MolangStaticNode(() -> value.getAsBoolean() ? 1.0F : 0.0F);
    }
}
