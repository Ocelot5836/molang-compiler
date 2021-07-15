package io.github.ocelot.molangcompiler.core.node;

import io.github.ocelot.molangcompiler.api.MolangEnvironment;
import io.github.ocelot.molangcompiler.api.MolangExpression;
import io.github.ocelot.molangcompiler.api.object.MolangObject;
import io.github.ocelot.molangcompiler.api.exception.MolangException;
import org.jetbrains.annotations.ApiStatus;

/**
 * @author Ocelot
 */
@ApiStatus.Internal
public class MolangInvokeFunctionNode implements MolangExpression
{
    private final String object;
    private final String name;
    private final MolangExpression[] parameters;

    public MolangInvokeFunctionNode(String object, String name, MolangExpression... parameters)
    {
        this.object = object;
        this.name = name;
        this.parameters = parameters;
    }

    @Override
    public float resolve(MolangEnvironment environment) throws MolangException
    {
        MolangObject object = environment.get(this.object);
        MolangExpression function;

        if (object.has(this.name + "$" + this.parameters.length))
        {
            function = object.get(this.name + "$" + this.parameters.length);
        }
        else if (object.has(this.name))
        {
            function = object.get(this.name);
        }
        else
        {
            throw new IllegalStateException("Unknown function: " + this.object + "." + this.name + "() with " + this.parameters.length + " parameters");
        }

        for (int i = 0; i < this.parameters.length; i++)
            environment.loadParameter(i, this.parameters[i]);
        float result = function.resolve(environment);
        environment.clearParameters();
        return result;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder(this.object).append('.').append(this.name).append('(');
        for (int i = 0; i < this.parameters.length; i++)
        {
            builder.append(this.parameters[i].toString());
            if (i < this.parameters.length - 1)
                builder.append(", ");
        }
        return builder.append(')').toString();
    }
}
