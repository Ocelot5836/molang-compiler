package io.github.ocelot.molangcompiler.core.node;

import io.github.ocelot.molangcompiler.api.MolangEnvironment;
import io.github.ocelot.molangcompiler.api.MolangExpression;
import io.github.ocelot.molangcompiler.api.exception.MolangException;

import java.util.function.Supplier;

/**
 * @author Ocelot
 */
public class MolangLazyNode implements MolangExpression
{
    private final Supplier<MolangExpression> parent;

    public MolangLazyNode(Supplier<MolangExpression> parent)
    {
        this.parent = new Supplier<MolangExpression>()
        {
            private MolangExpression object;

            @Override
            public MolangExpression get()
            {
                if (this.object == null)
                    this.object = parent.get();
                return object;
            }
        };
    }

    @Override
    public float resolve(MolangEnvironment environment) throws MolangException
    {
        return this.parent.get().resolve(environment);
    }

    @Override
    public String toString()
    {
        return String.valueOf(this.parent.get());
    }
}
