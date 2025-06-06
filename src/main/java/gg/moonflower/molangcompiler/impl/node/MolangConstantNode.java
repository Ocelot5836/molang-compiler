package gg.moonflower.molangcompiler.impl.node;

import gg.moonflower.molangcompiler.api.MolangEnvironment;
import gg.moonflower.molangcompiler.api.MolangExpression;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Ocelot
 */
@ApiStatus.Internal
public record MolangConstantNode(float value) implements MolangExpression {

    @Override
    public float get(@Nullable MolangEnvironment environment) {
        return this.value;
    }

    @Override
    public float getConstant() {
        return this.value;
    }

    @Override
    public boolean isConstant() {
        return true;
    }

    @Override
    public @NotNull String toString() {
        return Float.toString(this.value);
    }
}
