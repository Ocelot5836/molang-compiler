package gg.moonflower.molangcompiler.api.bridge;

import gg.moonflower.molangcompiler.api.exception.MolangRuntimeException;

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
    record Context(float[] parameters) {

        /**
         * Resolves the specified parameter.
         *
         * @param parameter The parameter to resolve
         * @return The float result of that parameter
         * @throws MolangRuntimeException If the expression could not be resolved
         */
        public float get(int parameter) throws MolangRuntimeException {
            if (parameter < 0 || parameter >= this.parameters.length) {
                throw new MolangRuntimeException("Invalid parameter: " + parameter);
            }
            return this.parameters[parameter];
        }

        /**
         * @return The number of parameters provided
         */
        public int getParameters() {
            return this.parameters.length;
        }
    }
}
