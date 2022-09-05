package io.github.ocelot.molangcompiler.api.bridge;

import io.github.ocelot.molangcompiler.api.MolangExpression;
import io.github.ocelot.molangcompiler.api.exception.MolangException;

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
     * @throws MolangException If any error occurs
     */
    float resolve(Context context) throws MolangException;

    /**
     * Provides parameters for MoLang Java functions.
     *
     * @author Ocelot
     * @since 1.0.0
     */
    interface Context {

        /**
         * Retrieves a value for the specified parameter
         *
         * @param parameter The parameter to get the value for
         * @return The parameter value
         * @throws MolangException If the parameter does not exist
         */
        MolangExpression get(int parameter) throws MolangException;

        /**
         * Resolves the specified parameter.
         *
         * @param parameter The parameter to resolve
         * @return The float result of that parameter
         * @throws MolangException If the expression could not be resolved
         */
        float resolve(int parameter) throws MolangException;

        /**
         * @return The number of parameters provided
         */
        int getParameters();
    }
}
