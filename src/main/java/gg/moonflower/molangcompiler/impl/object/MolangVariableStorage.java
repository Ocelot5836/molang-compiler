package gg.moonflower.molangcompiler.impl.object;

import gg.moonflower.molangcompiler.api.MolangExpression;
import gg.moonflower.molangcompiler.api.exception.MolangRuntimeException;
import gg.moonflower.molangcompiler.api.object.MolangObject;
import gg.moonflower.molangcompiler.impl.node.MolangFunctionNode;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Ocelot
 */
@ApiStatus.Internal
public class MolangVariableStorage implements MolangObject {

    private final Map<String, MolangExpression> storage;
    private final boolean allowMethods;

    public MolangVariableStorage(boolean allowMethods) {
        this.storage = new HashMap<>();
        this.allowMethods = allowMethods;
    }

    public MolangVariableStorage(MolangVariableStorage copy) {
        this.storage = new HashMap<>(copy.storage);
        this.allowMethods = copy.allowMethods;
    }

    public void clear() {
        this.storage.clear();
    }

    @Override
    public void set(String name, MolangExpression value) throws MolangRuntimeException {
        if (!this.allowMethods && value instanceof MolangFunctionNode) {
            throw new MolangRuntimeException("Cannot set functions on objects that do not allow functions");
        }
        this.storage.put(name, value);
    }

    @Override
    public void remove(String name) throws MolangRuntimeException {
        this.storage.remove(name);
    }

    @Override
    public MolangExpression get(String name) throws MolangRuntimeException {
        MolangExpression expression = this.storage.get(name);
        if (expression != null) {
            return expression;
        }
        throw new MolangRuntimeException("Unknown MoLang expression: " + name);
    }

    @Override
    public boolean has(String name) {
        return this.storage.containsKey(name);
    }

    @Override
    public Collection<String> getKeys() {
        return this.storage.keySet();
    }

    @Override
    public MolangObject getCopy() {
        return new MolangVariableStorage(this);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("MoLang Object\n");
        for (Map.Entry<String, MolangExpression> entry : this.storage.entrySet()) {
            builder.append('\t').append(entry.getKey());
            if (entry.getValue() instanceof MolangFunctionNode) {
                builder.append("()");
            } else {
                builder.append('=').append(entry.getValue());
            }
            builder.append('\n');
        }
        return builder.toString();
    }
}
