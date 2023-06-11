package io.github.ocelot.molangcompiler.core.node;

import io.github.ocelot.molangcompiler.api.MolangEnvironment;
import io.github.ocelot.molangcompiler.api.MolangExpression;
import io.github.ocelot.molangcompiler.api.bridge.MolangVariable;
import io.github.ocelot.molangcompiler.api.exception.MolangException;
import io.github.ocelot.molangcompiler.api.exception.MolangRuntimeException;
import io.github.ocelot.molangcompiler.api.object.MolangObject;
import org.jetbrains.annotations.ApiStatus;

import java.util.Objects;

/**
 * @author Ocelot
 */
@ApiStatus.Internal
public class MolangSetVariableNode implements MolangExpression {

    private final String object;
    private final String name;
    private final MolangExpression expression;

    public MolangSetVariableNode(String object, String name, MolangExpression expression) {
        this.object = object;
        this.name = name;
        this.expression = expression;
    }

    @Override
    public float get(MolangEnvironment environment) throws MolangRuntimeException {
        // This evaluates the value before setting it
        float value = this.expression.resolve(environment);

        MolangObject object = environment.get(this.object);
        MolangExpression old = object.get(this.name);
        if (old instanceof MolangVariable variable) {
            variable.setValue(value);
            return variable.getValue(); // Return the new value of the variable
        } else {
            object.set(this.name, new MolangConstantNode(value));
            return value;
        }
    }

    @Override
    public String toString() {
        return this.object + "." + this.name + " = " + this.expression;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MolangSetVariableNode that = (MolangSetVariableNode) o;
        return this.object.equals(that.object) && this.name.equals(that.name) && this.expression.equals(that.expression);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.object, this.name, this.expression);
    }
}
