package io.github.ocelot.molangcompiler.core.node;

import io.github.ocelot.molangcompiler.api.MolangEnvironment;
import io.github.ocelot.molangcompiler.api.MolangExpression;
import io.github.ocelot.molangcompiler.api.exception.MolangException;
import io.github.ocelot.molangcompiler.api.exception.MolangRuntimeException;
import org.jetbrains.annotations.ApiStatus;

import java.util.Objects;

/**
 * @author Ocelot
 */
@ApiStatus.Internal
public class MolangMathOperatorNode implements MolangExpression {

    private final MathOperation operation;
    private final MolangExpression a;
    private final MolangExpression b;

    public MolangMathOperatorNode(MathOperation operation, MolangExpression a, MolangExpression b) {
        this.operation = operation;
        this.a = a;
        this.b = b;
    }

    @Override
    public float get(MolangEnvironment environment) throws MolangRuntimeException {
        return this.operation.op.apply(this.a, this.b, environment);
    }

    @Override
    public String toString() {
        return "(" + this.a + " " + this.operation.sign + " " + this.b + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MolangMathOperatorNode that = (MolangMathOperatorNode) o;
        return this.operation == that.operation && this.a.equals(that.a) && this.b.equals(that.b);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.operation, this.a, this.b);
    }

    public enum MathOperation {
        MULTIPLY('*', (a, b, environment) -> a.resolve(environment) * b.resolve(environment)),
        DIVIDE('/', (a, b, environment) -> a.resolve(environment) / b.resolve(environment)),
        ADD('+', (a, b, environment) -> a.resolve(environment) + b.resolve(environment)),
        SUBTRACT('-', (a, b, environment) -> a.resolve(environment) - b.resolve(environment));

        private final char sign;
        private final MathOp op;

        MathOperation(char sign, MathOp op) {
            this.sign = sign;
            this.op = op;
        }
    }

    private interface MathOp {
        float apply(MolangExpression a, MolangExpression b, MolangEnvironment environment) throws MolangRuntimeException;
    }
}
