package io.github.ocelot.molangcompiler.core.node;

import io.github.ocelot.molangcompiler.api.MolangEnvironment;
import io.github.ocelot.molangcompiler.api.MolangExpression;
import io.github.ocelot.molangcompiler.api.exception.MolangException;

/**
 * @author Ocelot
 */
public class MolangCompoundNode implements MolangExpression
{
    private final MolangExpression[] expressions;

    public MolangCompoundNode(MolangExpression... expressions)
    {
        this.expressions = expressions;
    }

    @Override
    public float resolve(MolangEnvironment environment) throws MolangException
    {
        for (int i = 0; i < this.expressions.length; i++)
        {
            float result = this.expressions[i].resolve(environment);
            if (i >= this.expressions.length - 1) // The last expression is expected to have the `return`
                return result;
        }
        return 0;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < this.expressions.length; i++)
        {
            if (i >= this.expressions.length - 1)
                builder.append("return ");
            builder.append(this.expressions[i]);
            builder.append(';');
        }
        return builder.toString();
    }
}
