package gg.moonflower.molangcompiler.api.object;

import gg.moonflower.molangcompiler.api.MolangExpression;
import gg.moonflower.molangcompiler.api.exception.MolangRuntimeException;
import gg.moonflower.molangcompiler.impl.node.MolangFunctionNode;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * A template for a function-providing library in MoLang.
 *
 * @author Ocelot
 * @since 1.0.0
 */
public abstract class MolangLibrary implements MolangObject {

    private final Map<String, MolangExpression> values;

    public MolangLibrary() {
        this.values = new HashMap<>();
        this.populate(this.values::put);
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
    public void set(String name, MolangExpression value) throws MolangRuntimeException {
        throw new MolangRuntimeException("Cannot set values to " + this.getName());
    }

    @Override
    public void remove(String name) throws MolangRuntimeException {
        throw new MolangRuntimeException("Cannot remove values from " + this.getName());
    }

    @Override
    public MolangExpression get(String name) throws MolangRuntimeException {
        MolangExpression expression = this.values.get(name);
        if (expression != null) {
            return expression;
        }
        throw new MolangRuntimeException("Unknown MoLang expression: " + name);
    }

    @Override
    public boolean has(String name) {
        return this.values.containsKey(name);
    }

    @Override
    public Collection<String> getKeys() {
        return this.values.keySet();
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(this.getName()).append('\n');
        for (Map.Entry<String, MolangExpression> entry : this.values.entrySet()) {
            builder.append('\t').append(entry.getKey());
            if (entry.getValue() instanceof MolangFunctionNode) {
                builder.append("()");
            }
            builder.append('\n');
        }
        return builder.toString();
    }
}
