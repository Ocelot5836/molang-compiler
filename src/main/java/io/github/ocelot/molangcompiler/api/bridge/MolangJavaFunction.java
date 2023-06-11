package io.github.ocelot.molangcompiler.api.bridge;

import io.github.ocelot.molangcompiler.api.exception.MolangRuntimeException;

/**
 * Executes java code from MoLang expressions.
 *
 * @author Ocelot
 * @since 1.0.0
 */
@FunctionalInterface
public interface MolangJavaFunction {

    /**
     * Resolves a float from a set of parameters.
     *
     * @param context The parameters to execute using
     * @return The resulting float value
     * @throws MolangRuntimeException If any error occurs
     */
    float resolve(Context context) throws MolangRuntimeException;

    /**
     * Provides parameters for MoLang Java functions.
     *
     * @author Ocelot
     * @since 1.0.0
     */
    interface Context {

        /**
         * Resolves the specified parameter.
         *
         * @param parameter The parameter to resolve
         * @return The float result of that parameter
         * @throws MolangRuntimeException If the expression could not be resolved
         */
        float get(int parameter) throws MolangRuntimeException;

        /**
         * @return The number of parameters provided
         */
        int getParameters();
    }
}
