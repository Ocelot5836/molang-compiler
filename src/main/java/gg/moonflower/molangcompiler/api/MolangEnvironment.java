package gg.moonflower.molangcompiler.api;

import gg.moonflower.molangcompiler.api.exception.MolangRuntimeException;
import gg.moonflower.molangcompiler.api.object.MolangObject;

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
     * Loads a parameter into the next parameter slot.
     *
     * @param value The value to use as a parameter
     * @throws MolangRuntimeException If there is an issue loading the parameter
     * @since 3.0.0
     */
    void loadParameter(float value) throws MolangRuntimeException;

    /**
     * Clears all stored parameters.
     */
    void clearParameters();

    /**
     * @return The value of <code>this</code> in MoLang
     */
    float getThis();

    /**
     * Retrieves a {@link MolangObject} by the specified domain name.
     *
     * @param name The name to fetch by, case-insensitive
     * @return The object with the name
     * @throws MolangRuntimeException If the object does not exist
     */
    MolangObject get(String name) throws MolangRuntimeException;

    /**
     * Retrieves an expression by the specified parameter index.
     *
     * @param parameter The parameter to fetch
     * @return The parameter value
     * @throws MolangRuntimeException If the parameter does not exist
     */
    float getParameter(int parameter) throws MolangRuntimeException;

    /**
     * Checks to see if a parameter is loaded under the specified index.
     *
     * @param parameter The parameter to check
     * @return Whether a parameter is present
     */
    default boolean hasParameter(int parameter) {
        return parameter >= 0 && parameter < this.getParameters();
    }

    /**
     * @return The number of parameters loaded
     * @since 2.2.0
     */
    int getParameters();

    /**
     * Sets the value of "this".
     *
     * @param thisValue The new value
     * @since 2.0.0
     */
    void setThisValue(float thisValue);

    /**
     * <p>Resolves the float value of the specified expression in this environment.</p>
     * <p>This allows environments to fine-tune how expressions are evaluated.</p>
     *
     * @param expression The expression to evaluate
     * @return The resulting value
     * @throws MolangRuntimeException If any error occurs when resolving the value
     * @since 2.0.0
     */
    default float resolve(MolangExpression expression) throws MolangRuntimeException {
        return expression.get(this);
    }

    /**
     * <p>Resolves the float value of the specified expression in this environment. Catches any exception thrown and returns <code>0.0</code>.</p>
     * <p>This allows environments to fine-tune how expressions are evaluated.</p>
     *
     * @param expression The expression to evaluate
     * @return The resulting value
     * @since 2.0.0
     */
    default float safeResolve(MolangExpression expression) {
        try {
            return this.resolve(expression);
        } catch (Throwable t) {
            t.printStackTrace();
            return 0.0F;
        }
    }
}
