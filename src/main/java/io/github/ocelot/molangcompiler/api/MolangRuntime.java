package io.github.ocelot.molangcompiler.api;

import io.github.ocelot.molangcompiler.api.bridge.MolangJavaFunction;
import io.github.ocelot.molangcompiler.api.bridge.MolangVariable;
import io.github.ocelot.molangcompiler.api.bridge.MolangVariableProvider;
import io.github.ocelot.molangcompiler.api.exception.MolangRuntimeException;
import io.github.ocelot.molangcompiler.api.object.ImmutableMolangObject;
import io.github.ocelot.molangcompiler.api.object.MolangObject;
import io.github.ocelot.molangcompiler.core.object.MolangFunction;
import io.github.ocelot.molangcompiler.core.object.MolangVariableStorage;

import java.util.*;
import java.util.function.Supplier;

/**
 * The runtime for MoLang to create and access data from.
 *
 * @author Ocelot
 * @since 1.0.0
 */
public class MolangRuntime implements MolangEnvironment {

    private float thisValue;
    private final Map<String, MolangObject> objects;
    private final Set<String> aliases;
    private final List<Float> parameters;

    private MolangRuntime(MolangObject query, MolangObject global, MolangObject variable, Map<String, MolangObject> libraries) {
        this.thisValue = 0.0F;
        this.objects = new HashMap<>();
        this.aliases = new HashSet<>();
        this.objects.putAll(libraries);
        this.objects.put("context", query); // This is static accesses
        this.objects.put("query", query); // This is static accesses
        this.objects.put("global", global); // This is parameter access
        this.objects.put("variable", variable); // This can be accessed by Java code
        this.loadAlias("c", query); // Alias
        this.loadAlias("q", query); // Alias
        this.loadAlias("v", variable); // Alias
        this.parameters = new ArrayList<>(8);
    }

    /**
     * @return A dump of all objects stored in the runtime
     */
    public String dump() {
        StringBuilder builder = new StringBuilder("==Start MoLang Runtime Dump==\n\n");
        builder.append("==Start Objects==\n");
        for (Map.Entry<String, MolangObject> entry : this.objects.entrySet()) {
            if (!this.aliases.contains(entry.getKey())) {
                builder.append(entry.getKey()).append('=').append(entry.getValue()).append('\n');
            }
        }
        builder.deleteCharAt(builder.length() - 2);
        builder.append("==End Objects==\n\n");
        builder.append("==Start Parameters==\n");
        for (int i = 0; i < this.parameters.size(); i++) {
            builder.append("\tParameter ").append(i).append('=').append(this.parameters.get(i)).append('\n');
        }
        builder.append("==End Parameters==\n\n");
        builder.append("==End MoLang Runtime Dump==");
        return builder.toString();
    }

    @Override
    public void loadLibrary(String name, MolangObject object) {
        this.objects.put(name.toLowerCase(Locale.ROOT), object);
    }

    @Override
    public void loadAlias(String name, MolangObject object) {
        this.objects.put(name.toLowerCase(Locale.ROOT), object);
        this.aliases.add(name);
    }

    @Override
    public void loadParameter(float expression) {
        this.parameters.add(expression);
    }

    @Override
    public void clearParameters() {
        this.parameters.clear();
    }

    @Override
    public float getThis() {
        return this.thisValue;
    }

    @Override
    public MolangObject get(String name) throws MolangRuntimeException {
        name = name.toLowerCase(Locale.ROOT);
        if (!this.objects.containsKey(name)) {
            throw new MolangRuntimeException("Unknown MoLang object: " + name);
        }
        return this.objects.get(name);
    }

    @Override
    public float getParameter(int parameter) {
        if (parameter >= 0 && parameter < this.parameters.size()) {
            return this.parameters.get(parameter);
        }
        return 0;
    }

    @Override
    public int getParameters() {
        return this.parameters.size();
    }

    @Override
    public void setThisValue(float thisValue) {
        this.thisValue = thisValue;
    }

    /**
     * @return A new runtime builder.
     */
    public static Builder runtime() {
        return new Builder();
    }

    /**
     * Variables will be shared between the two runtimes, but new options added to the copy will not be reflected in the original.
     *
     * @return A new runtime builder copied from the specified builder.
     */
    public static Builder runtime(MolangRuntime.Builder copy) {
        return new Builder(copy);
    }

    /**
     * Constructs a new {@link MolangRuntime} with preset parameters.
     *
     * @author Ocelot
     * @since 1.0.0
     */
    public static class Builder {

        private final MolangVariableStorage query;
        private final MolangVariableStorage global;
        private final MolangVariableStorage variable;
        private final Map<String, MolangObject> libraries;

        public Builder() {
            this.query = new MolangVariableStorage(true);
            this.global = new MolangVariableStorage(true);
            this.variable = new MolangVariableStorage(false);
            this.libraries = new HashMap<>();
        }

        public Builder(Builder copy) {
            this.query = new MolangVariableStorage(copy.query);
            this.global = new MolangVariableStorage(copy.global);
            this.variable = new MolangVariableStorage(copy.variable);
            this.libraries = new HashMap<>(copy.libraries);
        }

        /**
         * Loads the specified object under the provided namespace. Ex. <code>libraryname.method()</code>
         *
         * @param name   The namespace of the library
         * @param object The library to load
         */
        public Builder loadLibrary(String name, MolangObject object) {
            this.libraries.put(name, object);
            return this;
        }

        /**
         * Sets a global immutable value.
         *
         * @param name  The name of the value
         * @param value The resulting number
         */
        public Builder setQuery(String name, MolangExpression value) {
            this.query.set(name, value);
            return this;
        }

        /**
         * Sets a global immutable value.
         *
         * @param name  The name of the value
         * @param value The resulting number
         */
        public Builder setQuery(String name, float value) {
            this.query.set(name, MolangExpression.of(value));
            return this;
        }

        /**
         * Sets a global immutable value that is lazily loaded.
         *
         * @param name  The name of the value
         * @param value The resulting number
         */
        public Builder setQuery(String name, Supplier<Float> value) {
            this.query.set(name, MolangExpression.lazy(value));
            return this;
        }

        /**
         * Sets a global immutable function.
         *
         * @param name     The name of the function
         * @param params   The number of parameters to accept or <code>-1</code> to accept any number
         * @param function The function to execute
         */
        public Builder setQuery(String name, int params, MolangJavaFunction function) {
            this.query.set(params < 0 ? name : (name + "$" + params), new MolangFunction(params, function));
            return this;
        }

        /**
         * Sets a global immutable value.
         *
         * @param name  The name of the value
         * @param value The resulting number
         */
        public Builder setGlobal(String name, MolangExpression value) {
            this.global.set(name, value);
            return this;
        }

        /**
         * Sets a global immutable value.
         *
         * @param name  The name of the value
         * @param value The resulting number
         */
        public Builder setGlobal(String name, float value) {
            this.global.set(name, MolangExpression.of(value));
            return this;
        }

        /**
         * Sets a global immutable value that is lazily loaded.
         *
         * @param name  The name of the value
         * @param value The resulting number
         */
        public Builder setGlobal(String name, Supplier<Float> value) {
            this.global.set(name, MolangExpression.lazy(value));
            return this;
        }

        /**
         * Sets a global immutable function.
         *
         * @param name     The name of the function
         * @param params   The number of parameters to accept
         * @param function The function to execute
         */
        public Builder setGlobal(String name, int params, MolangJavaFunction function) {
            this.global.set(params < 0 ? name : (name + "$" + params), new MolangFunction(params, function));
            return this;
        }

        /**
         * Sets a global mutable value.
         *
         * @param name  The name of the value
         * @param value The resulting number
         */
        public Builder setVariable(String name, MolangVariable value) {
            this.variable.set(name, MolangExpression.of(value)); // Variables are assumed to be used later
            return this;
        }

        /**
         * Sets a global mutable value.
         *
         * @param name  The name of the value
         * @param value The resulting number
         */
        public Builder setVariable(String name, MolangExpression value) {
            this.variable.set(name, value); // Variables are assumed to be used later
            return this;
        }

        /**
         * Adds all variables for the specified provider.
         *
         * @param provider The provider to add variables for
         */
        public Builder setVariables(MolangVariableProvider provider) {
            provider.addMolangVariables(new MolangVariableProvider.Context() {
                @Override
                public void addVariable(String name, MolangVariable value) {
                    setVariable(name, value);
                }

                @Override
                public void addQuery(String name, MolangExpression value) {
                    setQuery(name, value);
                }

                @Override
                public void addQuery(String name, float value) {
                    setQuery(name, value);
                }

                @Override
                public void addQuery(String name, Supplier<Float> value) {
                    setQuery(name, value);
                }

                @Override
                public void addQuery(String name, int params, MolangJavaFunction function) {
                    setQuery(name, params, function);
                }

                @Override
                public void removeVariable(String name) {
                    variable.set(name, MolangExpression.ZERO);
                }

                @Override
                public void removeQuery(String name) {
                    query.set(name, MolangExpression.ZERO);
                }
            });
            return this;
        }

        /**
         * @return A new runtime with <code>0</code> as the value for <code>this</code>.
         */
        public MolangRuntime create() {
            return new MolangRuntime(new ImmutableMolangObject(this.query), new ImmutableMolangObject(this.global), this.variable, this.libraries);
        }

        /**
         * Creates a new runtime with the provided value as the value for <code>this</code>.
         *
         * @param thisValue The value to load for the context
         * @return A new runtime
         */
        public MolangRuntime create(float thisValue) {
            MolangRuntime runtime = this.create();
            runtime.setThisValue(thisValue);
            return runtime;
        }

        /**
         * @return The query variable object
         */
        public MolangObject getQuery() {
            return this.query;
        }

        /**
         * @return The global variable object
         */
        public MolangObject getGlobal() {
            return this.global;
        }

        /**
         * @return The variable object
         */
        public MolangObject getVariable() {
            return this.variable;
        }
    }
}
