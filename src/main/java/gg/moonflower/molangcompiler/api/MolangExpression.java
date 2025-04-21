package gg.moonflower.molangcompiler.api;

import gg.moonflower.molangcompiler.api.bridge.MolangJavaFunction;
import gg.moonflower.molangcompiler.api.bridge.MolangVariable;
import gg.moonflower.molangcompiler.api.exception.MolangRuntimeException;
import gg.moonflower.molangcompiler.impl.node.*;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * A math expression that can be reduced using a {@link MolangEnvironment}.
 *
 * @author Ocelot
 * @see MolangEnvironment
 * @since 1.0.0
 */
@ApiStatus.NonExtendable
public interface MolangExpression {

    MolangExpression ZERO = of(0);

    /**
     * Resolves the float value of this expression.
     *
     * @param environment The environment to execute in
     * @return The resulting value
     * @throws MolangRuntimeException If any error occurs when resolving the value
     */
    @ApiStatus.Internal
    float get(MolangEnvironment environment) throws MolangRuntimeException;

    /**
     * Resolves the constant float value of this expression if {@link #isConstant()} returns <code>true</code>.
     *
     * @return The constant value backing this expression
     * @throws UnsupportedOperationException If this expression is not considered constant and cannot be resolved without an environment
     * @since 3.1.0
     */
    default float getConstant() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Expression is not constant");
    }

    /**
     * Resolves the float value of this expression.
     *
     * @param environment The environment to execute in
     * @return The resulting value
     * @throws MolangRuntimeException If any error occurs when resolving the value
     * @deprecated Use {@link MolangEnvironment#resolve(MolangExpression)}
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "4.0.0")
    default float resolve(MolangEnvironment environment) throws MolangRuntimeException {
        return environment.resolve(this);
    }

    /**
     * Resolves the float value of this expression. Catches any exception thrown and returns <code>0.0</code>.
     *
     * @param environment The environment to execute in
     * @return The resulting value
     * @deprecated Use {@link MolangEnvironment#safeResolve(MolangExpression)}
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "4.0.0")
    default float safeResolve(MolangEnvironment environment) {
        return environment.safeResolve(this);
    }

    /**
     * If this node is constant, {@link #getConstant()} can be safely called
     *
     * @return Whether this expression is considered constant and can be turned into a float primitive
     * @since 3.1.0
     */
    default boolean isConstant() {
        return false;
    }

    /**
     * Creates a copy of this expression if there is an internal state.
     *
     * @return A copy of this expression that has a unique internal state
     * @since 3.0.0
     * @deprecated Use {@link MolangExpression#createCopy()}
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "4.0.0")
    default MolangExpression getCopy() {
        return this.createCopy();
    }

    /**
     * Creates a copy of this expression if there is an internal state.
     *
     * @return A copy of this expression that has a unique internal state
     * @since 3.2.0
     */
    default MolangExpression createCopy() {
        return this;
    }

    /**
     * Creates a {@link MolangExpression} of the specified value.
     *
     * @param value The value to represent as an expression
     * @return A new expression with that value
     */
    static MolangExpression of(float value) {
        return new MolangConstantNode(value);
    }

    /**
     * Creates a {@link MolangExpression} of the specified value.
     *
     * @param value The value to represent as an expression
     * @return A new expression with that value
     */
    static MolangExpression of(boolean value) {
        return new MolangConstantNode(value ? 1.0F : 0.0F);
    }

    /**
     * Creates a {@link MolangExpression} of the specified value that will be computed after every call.
     *
     * @param value The value to represent as an expression
     * @return A new expression with that value
     */
    static MolangExpression of(Supplier<Float> value) {
        return new MolangDynamicNode(value);
    }

    /**
     * Creates a {@link MolangExpression} of the specified value that will be computed once and cached after.
     *
     * @param value The value to represent as an expression
     * @return A new expression with that value
     */
    static MolangExpression lazy(Supplier<Float> value) {
        return new MolangLazyNode(value);
    }

    /**
     * Creates a {@link MolangExpression} that calls the specified java code. It will only take the specified number of parameters.
     *
     * @param params   The number of parameters in the function. This must be at least 0
     * @param consumer The implementation of the MoLang call
     * @return A new expression that calls the java function
     * @since 3.1.0
     */
    static MolangExpression function(int params, MolangJavaFunction consumer) {
        return new MolangFunctionNode(params, consumer);
    }

    /**
     * Creates a {@link MolangExpression} that calls the specified java code. It will take any number of parameters.
     *
     * @param consumer The implementation of the MoLang call
     * @return A new expression that calls the java function
     * @since 3.1.0
     */
    static MolangExpression function(MolangJavaFunction consumer) {
        return new MolangFunctionNode(-1, consumer);
    }

    /**
     * Creates a {@link MolangExpression} of the specified value that will be computed after every call.
     *
     * @param value The value to represent as an expression
     * @return A new expression with that value
     */
    static MolangExpression of(BooleanSupplier value) {
        return new MolangDynamicNode(() -> value.getAsBoolean() ? 1.0F : 0.0F);
    }

    /**
     * Creates a {@link MolangExpression} of the specified value that will be computed once and cached after.
     *
     * @param value The value to represent as an expression
     * @return A new expression with that value
     */
    static MolangExpression lazy(BooleanSupplier value) {
        return new MolangLazyNode(() -> value.getAsBoolean() ? 1.0F : 0.0F);
    }

    /**
     * Creates a {@link MolangExpression} that access a Java variable.
     *
     * @param value The value to represent as an expression
     * @return A new expression with that value
     * @since 2.0.0
     */
    static MolangExpression of(MolangVariable value) {
        return new MolangVariableNode(value);
    }

    /**
     * Creates a {@link MolangExpression} that runs all expressions in order.
     *
     * @param expressions The expressions to represent as an expression
     * @return A new expression with that value
     * @since 3.0.0
     */
    static MolangExpression of(MolangExpression... expressions) {
        if (expressions.length == 0) {
            return MolangExpression.ZERO;
        }
        if (expressions.length == 1) {
            return expressions[0];
        }
        return new MolangCompoundNode(expressions);
    }
}
