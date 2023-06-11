package io.github.ocelot.molangcompiler.api.object;

import io.github.ocelot.molangcompiler.api.MolangExpression;
import io.github.ocelot.molangcompiler.api.exception.MolangRuntimeException;

/**
 * An object that can be referenced in MoLang.
 *
 * @author Ocelot
 * @since 1.0.0
 */
public interface MolangObject {

    /**
     * Retrieves a value with the specified name.
     *
     * @param name The name of the value to get
     * @return The value found
     */
    MolangExpression get(String name) throws MolangRuntimeException;

    /**
     * Sets a value with the specified name.
     *
     * @param name  The name of the value to set
     * @param value The value to set to the name
     */
    void set(String name, MolangExpression value) throws MolangRuntimeException;

    /**
     * Checks to see if there is a value with the specified name.
     *
     * @param name The name of the value to check
     * @return Whether a value exists with that name
     */
    boolean has(String name);
}
