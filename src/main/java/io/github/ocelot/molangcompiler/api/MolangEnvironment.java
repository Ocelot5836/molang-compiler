package io.github.ocelot.molangcompiler.api;

import io.github.ocelot.molangcompiler.api.exception.MolangException;
import io.github.ocelot.molangcompiler.api.object.MolangObject;

/**
 * A MoLang execution environment.
 *
 * @author Ocelot
 * @since 1.0.0
 */
public interface MolangEnvironment {

    /**
     * Loads a parameter into the specified slot.
     *
     * @param index      The parameter slot to load into
     * @param expression The expression to use as a parameter
     */
    void loadParameter(int index, MolangExpression expression) throws MolangException;

    /**
     * Clears all stored parameters.
     */
    void clearParameters() throws MolangException;

    /**
     * @return The value of <code>this</code> in MoLang
     */
    float getThis() throws MolangException;

    /**
     * Retrieves a {@link MolangObject} by the specified domain name.
     *
     * @param name The name to fetch by, case-insensitive
     * @return The object with the name
     * @throws MolangException If the object does not exist
     */
    MolangObject get(String name) throws MolangException;

    /**
     * Retrieves an expression by the specified parameter index.
     *
     * @param parameter The parameter to fetch
     * @return The parameter value or {@link MolangExpression#ZERO} if there is no parameter with that index
     */
    MolangExpression getParameter(int parameter) throws MolangException;

    /**
     * Checks to see if a parameter is loaded under the specified index.
     *
     * @param parameter The parameter to check
     * @return Whether a parameter is present
     */
    boolean hasParameter(int parameter) throws MolangException;
}
