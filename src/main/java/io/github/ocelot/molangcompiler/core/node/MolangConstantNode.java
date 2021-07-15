package io.github.ocelot.molangcompiler.core.node;

import io.github.ocelot.molangcompiler.api.MolangEnvironment;
import io.github.ocelot.molangcompiler.api.MolangExpression;
import org.jetbrains.annotations.ApiStatus;

/**
 * @author Ocelot
 */
@ApiStatus.Internal
public class MolangConstantNode implements MolangExpression
{
    private final float value;

    public MolangConstantNode(float value)
    {
        this.value = value;
    }

    @Override
    public float resolve(MolangEnvironment environment)
    {
        return value;
    }

    @Override
    public String toString()
    {
        return Float.toString(this.value);
    }
}
