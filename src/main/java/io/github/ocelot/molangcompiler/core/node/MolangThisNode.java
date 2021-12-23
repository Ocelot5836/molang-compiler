package io.github.ocelot.molangcompiler.core.node;

import io.github.ocelot.molangcompiler.api.MolangEnvironment;
import io.github.ocelot.molangcompiler.api.MolangExpression;
import io.github.ocelot.molangcompiler.api.exception.MolangException;
import org.jetbrains.annotations.ApiStatus;

/**
 * @author Ocelot
 */
@ApiStatus.Internal
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

    @Override
    public boolean equals(Object obj)
    {
        return obj instanceof MolangThisNode;
    }
}
