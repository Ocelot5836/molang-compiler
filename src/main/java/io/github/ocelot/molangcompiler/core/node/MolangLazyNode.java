package io.github.ocelot.molangcompiler.core.node;

import io.github.ocelot.molangcompiler.api.MolangEnvironment;
import io.github.ocelot.molangcompiler.api.MolangExpression;
import org.jetbrains.annotations.ApiStatus;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * @author Ocelot
 */
@ApiStatus.Internal
public class MolangLazyNode implements MolangExpression {

    private final Supplier<Float> value;
    private Float result = null;

    public MolangLazyNode(Supplier<Float> value) {
        this.value = () -> {
            if (this.result == null)
                this.result = value.get();
            return this.result;
        };
    }

    @Override
    public float get(MolangEnvironment environment) {
        return this.value.get();
    }

    @Override
    public String toString() {
        return Float.toString(this.value.get());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MolangLazyNode that = (MolangLazyNode) o;
        return this.value.get().equals(that.value.get());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.value.get());
    }
}
