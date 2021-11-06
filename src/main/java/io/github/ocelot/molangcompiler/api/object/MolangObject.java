package io.github.ocelot.molangcompiler.api.object;

import io.github.ocelot.molangcompiler.api.MolangExpression;

/**
 * An object that can be referenced in MoLang.
 *
 * @author Ocelot
 * @since 1.0.0
 */
public interface MolangObject
{
    /**
     * Sets a value with the specified name.
     *
     * @param name  The name of the value to set
     * @param value The value to set to the name
     */
    void set(String name, MolangExpression value);

    /**
     * Retrieves a value with the specified name.
     *
     * @param name The name of the value to get
     * @return The value or {@link MolangExpression#ZERO} if it doesn't exist
     */
    MolangExpression get(String name);

    /**
     * Checks to see if there is a value with the specified name.
     *
     * @param name The name of the value to check
     * @return Whether or not a value exists with that name
     */
    boolean has(String name);
}
