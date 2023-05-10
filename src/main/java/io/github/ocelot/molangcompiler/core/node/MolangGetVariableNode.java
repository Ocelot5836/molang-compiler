package io.github.ocelot.molangcompiler.core.node;

import io.github.ocelot.molangcompiler.api.MolangEnvironment;
import io.github.ocelot.molangcompiler.api.MolangExpression;
import io.github.ocelot.molangcompiler.api.exception.MolangException;
import org.jetbrains.annotations.ApiStatus;

import java.util.Objects;

/**
 * @author Ocelot
 */
@ApiStatus.Internal
public class MolangGetVariableNode implements MolangExpression {

    private final String object;
    private final String name;

    public MolangGetVariableNode(String object, String name) {
        this.object = object;
        this.name = name;
    }

    @Override
    public float get(MolangEnvironment environment) throws MolangException {
        return environment.get(this.object).get(this.name).resolve(environment);
    }

    @Override
    public String toString() {
        return this.object + "." + this.name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MolangGetVariableNode that = (MolangGetVariableNode) o;
        return this.object.equals(that.object) && this.name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.object, this.name);
    }
}
