package io.github.ocelot.molangcompiler.core.node;

import io.github.ocelot.molangcompiler.api.MolangEnvironment;
import io.github.ocelot.molangcompiler.api.MolangExpression;
import io.github.ocelot.molangcompiler.api.exception.MolangException;

/**
 * @author Ocelot
 */
public class MolangThisNode implements MolangExpression
{
    @Override
    public float resolve(MolangEnvironment environment) throws MolangException
    {
        return environment.getThis();
    }

    @Override
    public String toString()
    {
        return "this";
    }
}
