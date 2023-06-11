package io.github.ocelot.molangcompiler.core.node;

import io.github.ocelot.molangcompiler.api.MolangEnvironment;
import io.github.ocelot.molangcompiler.api.MolangExpression;
import io.github.ocelot.molangcompiler.api.exception.MolangException;
import io.github.ocelot.molangcompiler.api.exception.MolangRuntimeException;
import io.github.ocelot.molangcompiler.api.object.MolangObject;
import io.github.ocelot.molangcompiler.core.object.MolangVariableStack;
import org.jetbrains.annotations.ApiStatus;

import java.util.Objects;

/**
 * @author Ocelot
 */
@ApiStatus.Internal
public class MolangScopeNode implements MolangExpression {

    private final MolangExpression expression;

    public MolangScopeNode(MolangExpression expression) {
        this.expression = expression;
    }

    @Override
    public float get(MolangEnvironment environment) throws MolangRuntimeException {
        MolangObject object = environment.get("temp");
        if (object instanceof MolangVariableStack) {
            ((MolangVariableStack) object).push();
        }

        float result = this.expression.resolve(environment);
        if (object instanceof MolangVariableStack) {
            ((MolangVariableStack) object).pop();
        }
        return result;
    }

    @Override
    public String toString() {
        return "{\n" + this.expression + "\n}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MolangScopeNode that = (MolangScopeNode) o;
        return this.expression.equals(that.expression);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.expression);
    }
}
