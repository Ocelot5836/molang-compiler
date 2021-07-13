package io.github.ocelot.molangcompiler.core.node;

import io.github.ocelot.molangcompiler.api.MolangEnvironment;
import io.github.ocelot.molangcompiler.api.MolangExpression;
import io.github.ocelot.molangcompiler.api.exception.MolangException;

/**
 * @author Ocelot
 */
public class MolangMathOperatorNode implements MolangExpression
{
    private final MathOperation operation;
    private final MolangExpression a;
    private final MolangExpression b;

    public MolangMathOperatorNode(MathOperation operation, MolangExpression a, MolangExpression b)
    {
        this.operation = operation;
        this.a = a;
        this.b = b;
    }

    @Override
    public float resolve(MolangEnvironment environment) throws MolangException
    {
        return this.operation.op.apply(this.a, this.b, environment);
    }

    @Override
    public String toString()
    {
        return this.a + " " + this.operation.sign + " " + this.b;
    }

    public enum MathOperation
    {
        MULTIPLY('*', (a, b, environment) -> a.resolve(environment) * b.resolve(environment)),
        DIVIDE('/', (a, b, environment) ->
        {
            float second = b.resolve(environment);
            if (second == 0) // This is to prevent a divide by zero exception
                return 0;
            return a.resolve(environment) / second;
        }),
        ADD('+', (a, b, environment) -> a.resolve(environment) + b.resolve(environment)),
        SUBTRACT('-', (a, b, environment) -> a.resolve(environment) - b.resolve(environment));

        private final char sign;
        private final MathOp op;

        MathOperation(char sign, MathOp op)
        {
            this.sign = sign;
            this.op = op;
        }
    }

    private interface MathOp
    {
        float apply(MolangExpression a, MolangExpression b, MolangEnvironment environment) throws MolangException;
    }
}
