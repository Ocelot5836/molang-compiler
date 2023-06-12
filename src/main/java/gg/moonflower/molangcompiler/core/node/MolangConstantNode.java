package gg.moonflower.molangcompiler.core.node;

import gg.moonflower.molangcompiler.api.MolangEnvironment;
import gg.moonflower.molangcompiler.api.MolangExpression;
import org.jetbrains.annotations.ApiStatus;

/**
 * @author Ocelot
 */
@ApiStatus.Internal
public record MolangConstantNode(float value) implements MolangExpression {

    @Override
    public float get(MolangEnvironment environment) {
        return value;
    }

    @Override
    public String toString() {
        return Float.toString(this.value);
    }
}
