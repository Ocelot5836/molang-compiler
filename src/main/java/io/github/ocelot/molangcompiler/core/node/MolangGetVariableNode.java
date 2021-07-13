package io.github.ocelot.molangcompiler.core.node;

import io.github.ocelot.molangcompiler.api.MolangEnvironment;
import io.github.ocelot.molangcompiler.api.MolangExpression;
import io.github.ocelot.molangcompiler.api.exception.MolangException;

/**
 * @author Ocelot
 */
public class MolangGetVariableNode implements MolangExpression
{
    private final String object;
    private final String name;

    public MolangGetVariableNode(String object, String name)
    {
        this.object = object;
        this.name = name;
    }

    @Override
    public float resolve(MolangEnvironment environment) throws MolangException
    {
        return environment.get(this.object).get(this.name).resolve(environment);
    }

    @Override
    public String toString()
    {
        return this.object + "." + this.name;
    }
}
