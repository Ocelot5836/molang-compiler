package gg.moonflower.molangcompiler.api.object;

import gg.moonflower.molangcompiler.api.MolangExpression;
import gg.moonflower.molangcompiler.api.exception.MolangRuntimeException;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collection;

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
     * @throws MolangRuntimeException If the value does not exist. Use {@link #has(String)} to make sure the value exists.
     */
    MolangExpression get(String name) throws MolangRuntimeException;

    /**
     * Sets a value with the specified name.
     *
     * @param name  The name of the value to set
     * @param value The value to set to the name
     * @throws MolangRuntimeException If the value could not be set for any reason
     */
    void set(String name, MolangExpression value) throws MolangRuntimeException;

    /**
     * Removes a value with the specified name if it exists.
     *
     * @param name The name of the variable to remove
     * @throws MolangRuntimeException If the value could not be removed for any reason
     * @since 3.0.0
     */
    void remove(String name) throws MolangRuntimeException;

    /**
     * Checks to see if there is a value with the specified name.
     *
     * @param name The name of the value to check
     * @return Whether a value exists with that name
     */
    boolean has(String name);

    /**
     * Retrieves all keys for every value stored in this object.
     *
     * @return A collection containing all valid keys
     * @since 3.0.0
     */
    Collection<String> getKeys();

    /**
     * @return The version of this object that will be passed to all copies of the parent environment
     * @since 3.0.0
     * @deprecated Use {@link MolangObject#createCopy()}
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "4.0.0")
    default MolangObject getCopy() {
        return this;
    }

    /**
     * @return The version of this object that will be passed to all copies of the parent environment
     * @since 3.2.0
     */
    default MolangObject createCopy() {
        return this.getCopy();
    }

    /**
     * @return Whether this object is allowed to be mutated
     * @since 3.0.0
     */
    default boolean isMutable() {
        return true;
    }
}
