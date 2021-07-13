package io.github.ocelot.molangcompiler.core.object;

import io.github.ocelot.molangcompiler.api.MolangExpression;
import io.github.ocelot.molangcompiler.api.object.MolangObject;

import java.util.HashMap;
import java.util.Map;

public class MolangVariableStorage implements MolangObject
{
    private final Map<String, MolangExpression> storage;
    private final boolean allowMethods;

    public MolangVariableStorage(boolean allowMethods)
    {
        this.storage = new HashMap<>();
        this.allowMethods = allowMethods;
    }

    @Override
    public void set(String name, MolangExpression value)
    {
        if (value == MolangExpression.ZERO)
        {
            this.storage.remove(name);
            return;
        }
        if (!this.allowMethods && value instanceof MolangFunction)
            throw new IllegalStateException("Cannot set functions on objects that do not allow functions");
        this.storage.put(name, value);
    }

    @Override
    public MolangExpression get(String name)
    {
        return this.storage.getOrDefault(name, MolangExpression.ZERO);
    }

    @Override
    public boolean has(String name)
    {
        return this.storage.containsKey(name);
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder("MoLang Object\n");
        for (Map.Entry<String, MolangExpression> entry : this.storage.entrySet())
        {
            builder.append('\t').append(entry.getKey());
            if (entry.getValue() instanceof MolangFunction)
            {
                builder.append("()");
            }
            else
            {
                builder.append('=').append(entry.getValue());
            }
            builder.append('\n');
        }
        return builder.toString();
    }
}
