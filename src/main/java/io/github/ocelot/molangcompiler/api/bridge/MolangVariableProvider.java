package io.github.ocelot.molangcompiler.api.bridge;

/**
 * <p>Provides variables for Molang expressions. Variables are immutable values from Java code used as constants in MoLang.</p>
 *
 * @author Ocelot
 * @since 1.0.0
 */
@FunctionalInterface
public interface MolangVariableProvider
{
    /**
     * Modifies all variables to the provided context.
     *
     * @param context The variable modification context
     */
    void addMolangVariables(Context context);

    /**
     * <p>Context for MoLang variable modification.</p>
     *
     * @author Ocelot
     * @since 1.0.0
     */
    interface Context
    {
        /**
         * Adds a value to the variable struct.
         *
         * @param name  The name of the variable to set
         * @param value The value to set under that name
         */
        void add(String name, float value);

        /**
         * Removes a variable with the specified name.
         *
         * @param name The name of the variable to remove
         */
        void remove(String name);
    }
}
