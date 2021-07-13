package io.github.ocelot.molangcompiler.api;


import io.github.ocelot.molangcompiler.api.exception.MolangException;
import io.github.ocelot.molangcompiler.core.node.MolangConstantNode;

/**
 * <p>A math expression that can be reduced using a {@link MolangRuntime}.</p>
 *
 * @author Ocelot
 * @since 1.0.0
 */
public interface MolangExpression
{
    MolangExpression ZERO = new MolangConstantNode(0);

    /**
     * Resolves the float value of this runtime.
     *
     * @param environment The environment to execute in
     * @return The resulting value
     * @throws MolangException If any error occurs when resolving the value
     */
    float resolve(MolangEnvironment environment) throws MolangException;

    /**
     * Resolves the float value of this runtime. Safely catches any exception thrown and returns <code>0</code>.
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
}
