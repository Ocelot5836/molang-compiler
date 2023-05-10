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
public class MolangCompareNode implements MolangExpression {

    private final MolangExpression first;
    private final MolangExpression second;
    private final CompareMode mode;

    public MolangCompareNode(MolangExpression first, MolangExpression second, CompareMode mode) {
        this.first = first;
        this.second = second;
        this.mode = mode;
    }

    @Override
    public float get(MolangEnvironment environment) throws MolangException {
        return this.mode.resolve(this.first, this.second, environment);
    }

    @Override
    public String toString() {
        return this.first + " " + this.mode.sign + " " + this.second;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MolangCompareNode that = (MolangCompareNode) o;
        return first.equals(that.first) && second.equals(that.second) && mode == that.mode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second, mode);
    }

    public enum CompareMode {
        GREATER(">", (a, b, environment) -> a.resolve(environment) > b.resolve(environment)),
        GEQUAL(">=", (a, b, environment) -> a.resolve(environment) >= b.resolve(environment)),
        LESS("<", (a, b, environment) -> a.resolve(environment) < b.resolve(environment)),
        LEQUAL("<=", (a, b, environment) -> a.resolve(environment) <= b.resolve(environment)),
        EQUUAL("==", (a, b, environment) -> a.resolve(environment) == b.resolve(environment));

        private final String sign;
        private final CompareOp op;

        CompareMode(String sign, CompareOp op) {
            this.sign = sign;
            this.op = op;
        }

        public float resolve(MolangExpression a, MolangExpression b, MolangEnvironment environment) throws MolangException {
            return this.op.apply(a, b, environment) ? 1.0F : 0.0F;
        }

        public String getSign() {
            return sign;
        }
    }

    @FunctionalInterface
    private interface CompareOp {
        boolean apply(MolangExpression a, MolangExpression b, MolangEnvironment environment) throws MolangException;
    }
}
