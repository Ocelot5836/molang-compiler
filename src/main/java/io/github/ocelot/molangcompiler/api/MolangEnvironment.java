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
     * Loads a library under the specified name.
     *
     * @param name   The name of the library to load
     * @param object The object to use under that name
     * @since 2.0.0
     */
    void loadLibrary(String name, MolangObject object);

    /**
     * Loads an alias for a library under the specified name.
     *
     * @param name   The name of the library to load
     * @param object The object to use under that name
     * @since 2.0.0
     */
    void loadAlias(String name, MolangObject object);

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

    /**
     * Sets the value of "this."
     *
     * @param thisValue The new value
     * @since 2.0.0
     */
    void setThisValue(float thisValue);

    /**
     * Resolves the float value of the specified expression in this environment.
     * <p>This allows environments to fine-tune how expressions are evaluated.
     *
     * @param expression The expression to evaluate
     * @return The resulting value
     * @throws MolangException If any error occurs when resolving the value
     * @since 2.0.0
     */
    default float resolve(MolangExpression expression) throws MolangException {
        return expression.get(this);
    }

    /**
     * Resolves the float value of the specified expression in this environment. Catches any exception thrown and returns <code>0.0</code>.
     * <p>This allows environments to fine-tune how expressions are evaluated.
     *
     * @param expression The expression to evaluate
     * @return The resulting value
     * @since 2.0.0
     */
    default float safeResolve(MolangExpression expression) {
        try {
            return this.resolve(expression);
        } catch (MolangException e) {
            e.printStackTrace();
            return 0.0F;
        }
    }
}
