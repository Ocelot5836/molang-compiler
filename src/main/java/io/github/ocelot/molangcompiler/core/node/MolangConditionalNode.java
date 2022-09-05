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
public class MolangConditionalNode implements MolangExpression {

    private final MolangExpression condition;
    private final MolangExpression first;
    private final MolangExpression branch;

    public MolangConditionalNode(MolangExpression condition, MolangExpression first, MolangExpression branch) {
        this.condition = condition;
        this.first = first;
        this.branch = branch;
    }

    @Override
    public float resolve(MolangEnvironment environment) throws MolangException {
        return (this.condition.resolve(environment) != 0 ? this.first : this.branch).resolve(environment);
    }

    @Override
    public String toString() {
        return this.condition + " ? " + this.first + " : " + this.branch;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MolangConditionalNode that = (MolangConditionalNode) o;
        return this.condition.equals(that.condition) && this.first.equals(that.first) && this.branch.equals(that.branch);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.condition, this.first, this.branch);
    }
}
