package io.github.ocelot.molangcompiler.core.node;

import io.github.ocelot.molangcompiler.api.MolangEnvironment;
import io.github.ocelot.molangcompiler.api.MolangExpression;
import io.github.ocelot.molangcompiler.api.exception.MolangException;
import io.github.ocelot.molangcompiler.api.object.MolangObject;
import org.jetbrains.annotations.ApiStatus;

import java.util.Arrays;
import java.util.Objects;

/**
 * @author Ocelot
 */
@ApiStatus.Internal
public class MolangInvokeFunctionNode implements MolangExpression {

    private final String object;
    private final String name;
    private final MolangExpression[] parameters;

    public MolangInvokeFunctionNode(String object, String name, MolangExpression... parameters) {
        this.object = object;
        this.name = name;
        this.parameters = parameters;
    }

    @Override
    public float get(MolangEnvironment environment) throws MolangException {
        MolangObject object = environment.get(this.object);
        MolangExpression function;

        if (object.has(this.name + "$" + this.parameters.length)) {
            function = object.get(this.name + "$" + this.parameters.length);
        } else if (object.has(this.name)) {
            function = object.get(this.name);
        } else {
            throw new IllegalStateException("Unknown function: " + this.object + "." + this.name + "() with " + this.parameters.length + " parameters");
        }

        for (int i = 0; i < this.parameters.length; i++)
            environment.loadParameter(i, this.parameters[i]);
        float result = function.resolve(environment);
        environment.clearParameters();
        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(this.object).append('.').append(this.name).append('(');
        for (int i = 0; i < this.parameters.length; i++) {
            builder.append(this.parameters[i].toString());
            if (i < this.parameters.length - 1)
                builder.append(", ");
        }
        return builder.append(')').toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MolangInvokeFunctionNode that = (MolangInvokeFunctionNode) o;
        return this.object.equals(that.object) && this.name.equals(that.name) && Arrays.equals(this.parameters, that.parameters);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(this.object, this.name);
        result = 31 * result + Arrays.hashCode(this.parameters);
        return result;
    }
}
