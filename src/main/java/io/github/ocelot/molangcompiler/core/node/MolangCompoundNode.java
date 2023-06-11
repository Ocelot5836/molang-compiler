package io.github.ocelot.molangcompiler.core.node;

import io.github.ocelot.molangcompiler.api.MolangEnvironment;
import io.github.ocelot.molangcompiler.api.MolangExpression;
import io.github.ocelot.molangcompiler.api.exception.MolangRuntimeException;
import org.jetbrains.annotations.ApiStatus;

/**
 * @author Ocelot
 */
@ApiStatus.Internal
public record MolangCompoundNode(MolangExpression... expressions) implements MolangExpression {

    @Override
    public float get(MolangEnvironment environment) throws MolangRuntimeException {
        for (int i = 0; i < this.expressions.length; i++) {
            float result = environment.resolve(this.expressions[i]);
            // The last expression is expected to have the `return`
            if (i >= this.expressions.length - 1) {
                return result;
            }
        }
        return 0;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < this.expressions.length; i++) {
            if (i >= this.expressions.length - 1) {
                builder.append("return ");
            }
            builder.append(this.expressions[i]);
            builder.append(';');
            if (i < this.expressions.length - 1) {
                builder.append('\n');
            }
        }
        return builder.toString();
    }
}
