package io.github.ocelot.molangcompiler.core.node;

import io.github.ocelot.molangcompiler.api.MolangEnvironment;
import io.github.ocelot.molangcompiler.api.MolangExpression;
import org.jetbrains.annotations.ApiStatus;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MolangConstantNode that = (MolangConstantNode) o;
        return Float.compare(that.value, this.value) == 0;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(this.value);
    }
}
