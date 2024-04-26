package gg.moonflower.molangcompiler.api;

import gg.moonflower.molangcompiler.api.bridge.MolangJavaFunction;
import gg.moonflower.molangcompiler.api.bridge.MolangVariable;
import gg.moonflower.molangcompiler.api.bridge.MolangVariableProvider;
import gg.moonflower.molangcompiler.api.object.MolangObject;

import java.util.function.Supplier;

/**
 * Constructs a MoLang environment by specifying the initial state.
 *
 * @param <V> The type of environment constructed
 * @author Ocelot
 * @since 3.0.0
 */
public interface MolangEnvironmentBuilder<V extends MolangEnvironment> {

    /**
     * Loads the specified object under the provided namespace. Ex. <code>libraryname.method()</code>
     *
     * @param name   The namespace of the library
     * @param object The library to load
     */
    MolangEnvironmentBuilder<V> loadLibrary(String name, MolangObject object);

    /**
     * Unloads the specified object from the provided namespace. Ex. <code>libraryname.method()</code>
     *
     * @param name The namespace of the library
     */
    MolangEnvironmentBuilder<V> unloadLibrary(String name);

    /**
     * Sets a global immutable value.
     *
     * @param name  The name of the value
     * @param value The resulting number
     */
    MolangEnvironmentBuilder<V> setQuery(String name, MolangExpression value);

    /**
     * Sets a global immutable value.
     *
     * @param name  The name of the value
     * @param value The resulting number
     */
    default MolangEnvironmentBuilder<V> setQuery(String name, float value) {
        return this.setQuery(name, MolangExpression.of(value));
    }

    /**
     * Sets a global immutable value that is lazily loaded.
     *
     * @param name  The name of the value
     * @param value The resulting number
     */
    default MolangEnvironmentBuilder<V> setQuery(String name, Supplier<Float> value) {
        return this.setQuery(name, MolangExpression.lazy(value));
    }

    /**
     * Sets a global immutable function.
     *
     * @param name     The name of the function
     * @param params   The number of parameters to accept or <code>-1</code> to accept any number
     * @param function The function to execute
     */
    default MolangEnvironmentBuilder<V> setQuery(String name, int params, MolangJavaFunction function) {
        return this.setQuery(params < 0 ? name : (name + "$" + params), MolangExpression.function(params, function));
    }

    /**
     * Sets a global immutable value.
     *
     * @param name  The name of the value
     * @param value The resulting number
     */
    MolangEnvironmentBuilder<V> setGlobal(String name, MolangExpression value);

    /**
     * Sets a global immutable value.
     *
     * @param name  The name of the value
     * @param value The resulting number
     */
    default MolangEnvironmentBuilder<V> setGlobal(String name, float value) {
        return this.setGlobal(name, MolangExpression.of(value));
    }

    /**
     * Sets a global immutable value that is lazily loaded.
     *
     * @param name  The name of the value
     * @param value The resulting number
     */
    default MolangEnvironmentBuilder<V> setGlobal(String name, Supplier<Float> value) {
        return this.setGlobal(name, MolangExpression.lazy(value));
    }

    /**
     * Sets a global immutable function.
     *
     * @param name     The name of the function
     * @param params   The number of parameters to accept
     * @param function The function to execute
     */
    default MolangEnvironmentBuilder<V> setGlobal(String name, int params, MolangJavaFunction function) {
        return this.setGlobal(params < 0 ? name : (name + "$" + params), MolangExpression.function(params, function));
    }

    /**
     * Sets a global mutable value.
     *
     * @param name  The name of the value
     * @param value The resulting number
     */
    MolangEnvironmentBuilder<V> setVariable(String name, MolangExpression value);

    /**
     * Sets a global mutable value.
     *
     * @param name  The name of the value
     * @param value The resulting number
     */
    default MolangEnvironmentBuilder<V> setVariable(String name, MolangVariable value) {
        return this.setVariable(name, MolangExpression.of(value));
    }

    /**
     * Sets a global immutable value.
     *
     * @param name  The name of the value
     * @param value The resulting number
     */
    default MolangEnvironmentBuilder<V> setVariable(String name, float value) {
        return this.setVariable(name, MolangExpression.of(value));
    }

    /**
     * Sets a global immutable value that is lazily loaded.
     *
     * @param name  The name of the value
     * @param value The resulting number
     */
    default MolangEnvironmentBuilder<V> setVariable(String name, Supplier<Float> value) {
        return this.setVariable(name, MolangExpression.lazy(value));
    }

    /**
     * Removes a query with the specified name.
     *
     * @param name The name of the value to remove
     */
    MolangEnvironmentBuilder<V> removeQuery(String name);

    /**
     * Removes a global value with the specified name.
     *
     * @param name The name of the value to remove
     */
    MolangEnvironmentBuilder<V> removeGlobal(String name);

    /**
     * Removes a global mutable with the specified name.
     *
     * @param name The name of the value to remove
     */
    MolangEnvironmentBuilder<V> removeVariable(String name);

    /**
     * Removes all extra libraries.
     */
    MolangEnvironmentBuilder<V> clearLibraries();

    /**
     * Removes all query values.
     */
    MolangEnvironmentBuilder<V> clearQuery();

    /**
     * Removes all global values.
     */
    MolangEnvironmentBuilder<V> clearGlobal();

    /**
     * Removes all variable values.
     */
    MolangEnvironmentBuilder<V> clearVariable();

    /**
     * Attempts to copy the environment state from the specified environment.
     * @param environment The environment to copy from
     */
    MolangEnvironmentBuilder<V> copy(MolangEnvironment environment);

    /**
     * Adds all variables for the specified provider.
     *
     * @param provider The provider to add variables for
     */
    default MolangEnvironmentBuilder<V> setVariables(MolangVariableProvider provider) {
        provider.addMolangVariables(new MolangVariableProvider.Context() {
            @Override
            public void addVariable(String name, MolangVariable value) {
                MolangEnvironmentBuilder.this.setVariable(name, value);
            }

            @Override
            public void addQuery(String name, MolangExpression value) {
                MolangEnvironmentBuilder.this.setQuery(name, value);
            }

            @Override
            public void addQuery(String name, float value) {
                MolangEnvironmentBuilder.this.setQuery(name, value);
            }

            @Override
            public void addQuery(String name, Supplier<Float> value) {
                MolangEnvironmentBuilder.this.setQuery(name, value);
            }

            @Override
            public void addQuery(String name, int params, MolangJavaFunction function) {
                MolangEnvironmentBuilder.this.setQuery(name, params, function);
            }

            @Override
            public void removeQuery(String name) {
                MolangEnvironmentBuilder.this.removeQuery(name);
            }

            @Override
            public void removeVariable(String name) {
                MolangEnvironmentBuilder.this.removeVariable(name);
            }
        });
        return this;
    }

    /**
     * @return A new runtime with <code>0</code> as the value for <code>this</code>.
     */
    V create();

    /**
     * Creates a new runtime with the provided value as the value for <code>this</code>.
     *
     * @param thisValue The value to load for the context
     * @return A new runtime
     */
    default V create(float thisValue) {
        V environment = this.create();
        environment.setThisValue(thisValue);
        return environment;
    }
}
