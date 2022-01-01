package io.github.ocelot.molangcompiler.api.bridge;

import org.jetbrains.annotations.ApiStatus;

import java.util.function.Supplier;

/**
 * Provides variables for Molang expressions. Variables are immutable values from Java code used as constants in MoLang.
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

    // TODO remove deprecated features in 2.0.0

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
         * @deprecated Use {@link #addVariable(String, float)} instead
         */
        @Deprecated
        @ApiStatus.ScheduledForRemoval(inVersion = "2.0.0")
        default void add(String name, float value)
        {
            this.addVariable(name, value);
        }

        /**
         * Removes a variable with the specified name.
         *
         * @param name The name of the variable to remove
         * @deprecated Use {@link #removeVariable(String)} instead
         */
        @Deprecated
        @ApiStatus.ScheduledForRemoval(inVersion = "2.0.0")
        default void remove(String name)
        {
            this.removeVariable(name);
        }

        /**
         * Adds a value to the variable struct.
         *
         * @param name  The name of the variable to set
         * @param value The value to set under that name
         */
        void addVariable(String name, float value);

        /**
         * Sets a global immutable value.
         *
         * @param name  The name of the value
         * @param value The resulting number
         */
        void addQuery(String name, float value);

        /**
         * Sets a global immutable value that is lazily loaded.
         *
         * @param name  The name of the value
         * @param value The resulting number
         */
        void addQuery(String name, Supplier<Float> value);

        /**
         * Sets a global immutable function.
         *
         * @param name     The name of the function
         * @param params   The number of parameters to accept
         * @param function The function to execute
         */
        void addQuery(String name, int params, MolangJavaFunction function);

        /**
         * Removes a variable with the specified name.
         *
         * @param name The name of the variable to remove
         */
        void removeVariable(String name);

        /**
         * Removes a query with the specified name.
         *
         * @param name The name of the query to remove
         */
        void removeQuery(String name);
    }
}
