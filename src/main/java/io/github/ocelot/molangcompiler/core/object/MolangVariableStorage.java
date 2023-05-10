package io.github.ocelot.molangcompiler.core.object;

import io.github.ocelot.molangcompiler.api.MolangExpression;
import io.github.ocelot.molangcompiler.api.object.MolangObject;
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

    protected Map<String, MolangExpression> getStorage() {
        return storage;
    }

    @Override
    public void set(String name, MolangExpression value) {
        if (value.equals(MolangExpression.ZERO)) {
            this.getStorage().remove(name);
            return;
        }
        if (!this.allowMethods && value instanceof MolangFunction) {
            throw new IllegalStateException("Cannot set functions on objects that do not allow functions");
        }
        this.getStorage().put(name, value);
    }

    @Override
    public MolangExpression get(String name) {
        return this.getStorage().getOrDefault(name, MolangExpression.ZERO);
    }

    @Override
    public boolean has(String name) {
        return this.getStorage().containsKey(name);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("MoLang Object\n");
        for (Map.Entry<String, MolangExpression> entry : this.getStorage().entrySet()) {
            builder.append('\t').append(entry.getKey());
            if (entry.getValue() instanceof MolangFunction) {
                builder.append("()");
            } else {
                builder.append('=').append(entry.getValue());
            }
            builder.append('\n');
        }
        return builder.toString();
    }
}
