package gg.moonflower.molangcompiler.api.bridge;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Provides variables for Molang expressions. Variables are mutable values from Java code that can be modified in MoLang.
 *
 * @author Ocelot
 * @since 2.0.0
 */
public interface MolangVariable {

    /**
     * @return The value of this variable
     */
    float getValue();

    /**
     * Sets this variable from MoLang.
     *
     * @param value The new value
     */
    void setValue(float value);

    /**
     * @return A new copy of this variable
     * @since 3.0.0
     */
    MolangVariable copy();

    /**
     * Makes read-only copy of this variable.
     *
     * @return A new variable that reflects the value of the provided, but cannot be changed
     * @since 2.0.0
     */
    default MolangVariable immutable() {
        return new MolangVariable() {
            @Override
            public float getValue() {
                return MolangVariable.this.getValue();
            }

            @Override
            public void setValue(float value) {
            }

            @Override
            public MolangVariable copy() {
                return this;
            }

            @Override
            public MolangVariable immutable() {
                return this;
            }

            @Override
            public String toString() {
                return "ImmutableMolangVariable[value=" + this.getValue() + "]";
            }
        };
    }

    /**
     * Helper for creating a MoLang variable.
     *
     * @param getter The getter for the value
     * @param setter The setter for the value
     * @return The variable representation
     */
    static MolangVariable of(Supplier<Float> getter, Consumer<Float> setter) {
        return new MolangVariable() {
            @Override
            public float getValue() {
                return getter.get();
            }

            @Override
            public void setValue(float value) {
                setter.accept(value);
            }

            @Override
            public MolangVariable copy() {
                return this;
            }

            @Override
            public String toString() {
                return "DynamicMolangVariable[value=" + this.getValue() + "]";
            }
        };
    }

    /**
     * Helper for creating a MoLang variable without a backing field.
     *
     * @return A private variable that can be retrieved
     */
    static MolangVariable create() {
        return create(0.0F);
    }

    /**
     * Helper for creating a MoLang variable without a backing field.
     *
     * @param initialValue The initial value of the variable
     * @return A private variable that can be retrieved
     */
    static MolangVariable create(float initialValue) {
        final float[] value = {initialValue};
        return new MolangVariable() {
            @Override
            public float getValue() {
                return value[0];
            }

            @Override
            public void setValue(float v) {
                value[0] = v;
            }

            @Override
            public MolangVariable copy() {
                return create(value[0]);
            }

            @Override
            public String toString() {
                return "MolangVariable[value=" + this.getValue() + "]";
            }
        };
    }
}
