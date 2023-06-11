package io.github.ocelot.molangcompiler.core.node;

import io.github.ocelot.molangcompiler.api.MolangEnvironment;
import io.github.ocelot.molangcompiler.api.MolangExpression;
import io.github.ocelot.molangcompiler.api.bridge.MolangVariable;
import org.jetbrains.annotations.ApiStatus;

/**
 * @author Ocelot
 */
@ApiStatus.Internal
public record MolangVariableNode(MolangVariable value) implements MolangExpression, MolangVariable {

    @Override
    public float get(MolangEnvironment environment) {
        return this.value.getValue();
    }

    @Override
    public String toString() {
        return Float.toString(this.value.getValue());
    }

    @Override
    public float getValue() {
        return this.value.getValue();
    }

    @Override
    public void setValue(float value) {
        this.value.setValue(value);
    }
}
