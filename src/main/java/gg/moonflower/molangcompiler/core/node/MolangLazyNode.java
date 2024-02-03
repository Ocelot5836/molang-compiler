package gg.moonflower.molangcompiler.core.node;

import gg.moonflower.molangcompiler.api.MolangEnvironment;
import gg.moonflower.molangcompiler.api.MolangExpression;
import org.jetbrains.annotations.ApiStatus;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * @author Ocelot
 */
@ApiStatus.Internal
public class MolangLazyNode implements MolangExpression {

    private final Supplier<Float> value;

    public MolangLazyNode(Supplier<Float> value) {
        this.value = new Supplier<>() {
            private Float result = null;

            @Override
            public Float get() {
                if (this.result == null) {
                    this.result = value.get();
                }
                return this.result;
            }
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
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        MolangLazyNode that = (MolangLazyNode) o;
        return this.value.get().equals(that.value.get());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.value.get());
    }
}
