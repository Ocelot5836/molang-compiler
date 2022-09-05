package io.github.ocelot.molangcompiler.core.node;

import io.github.ocelot.molangcompiler.api.MolangEnvironment;
import io.github.ocelot.molangcompiler.api.MolangExpression;
import io.github.ocelot.molangcompiler.api.bridge.MolangVariable;
import org.jetbrains.annotations.ApiStatus;

import java.util.Objects;

/**
 * @author Ocelot
 */
@ApiStatus.Internal
public class MolangStaticNode implements MolangExpression, MolangVariable {

    private final MolangVariable value;

    public MolangStaticNode(MolangVariable value) {
        this.value = value;
    }

    @Override
    public float resolve(MolangEnvironment environment) {
        return this.value.getValue();
    }

    @Override
    public String toString() {
        return Float.toString(this.value.getValue());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MolangStaticNode that = (MolangStaticNode) o;
        return this.value.getValue() == that.value.getValue();
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.value.getValue());
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
