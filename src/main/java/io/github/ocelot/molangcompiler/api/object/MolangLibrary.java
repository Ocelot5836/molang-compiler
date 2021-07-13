package io.github.ocelot.molangcompiler.api.object;

import io.github.ocelot.molangcompiler.api.MolangExpression;
import io.github.ocelot.molangcompiler.core.object.MolangFunction;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * <p>A template for a function-providing library in MoLang.</p>
 *
 * @author Ocelot
 * @since 1.0.0
 */
public abstract class MolangLibrary implements MolangObject
{
    private final Map<String, MolangExpression> functions;

    public MolangLibrary()
    {
        this.functions = new HashMap<>();
        this.populate(this.functions::put);
    }

    /**
     * Populates the values this library will provide.
     *
     * @param consumer The consumer for functions
     */
    protected abstract void populate(BiConsumer<String, MolangExpression> consumer);

    /**
     * @return The name of this library
     */
    protected abstract String getName();

    @Override
    public void set(String name, MolangExpression value)
    {
        throw new UnsupportedOperationException("Cannot set values on a library");
    }

    @Override
    public MolangExpression get(String name)
    {
        return this.functions.getOrDefault(name, MolangExpression.ZERO);
    }

    @Override
    public boolean has(String name)
    {
        return this.functions.containsKey(name);
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder(this.getName()).append('\n');
        for (Map.Entry<String, MolangExpression> entry : this.functions.entrySet())
        {
            builder.append('\t').append(entry.getKey());
            if (entry.getValue() instanceof MolangFunction)
                builder.append("()");
            builder.append('\n');
        }
        return builder.toString();
    }
}
