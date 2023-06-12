package gg.moonflower.molangcompiler.core.object;

import gg.moonflower.molangcompiler.api.MolangExpression;
import gg.moonflower.molangcompiler.api.exception.MolangRuntimeException;
import gg.moonflower.molangcompiler.api.object.MolangObject;
import gg.moonflower.molangcompiler.core.node.MolangFunctionNode;
import org.jetbrains.annotations.ApiStatus;

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

    @Override
    public void set(String name, MolangExpression value) {
        if (!this.allowMethods && value instanceof MolangFunctionNode) {
            throw new IllegalStateException("Cannot set functions on objects that do not allow functions");
        }
        this.storage.put(name, value);
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
