package gg.moonflower.molangcompiler.api;

import gg.moonflower.molangcompiler.api.exception.MolangRuntimeException;
import gg.moonflower.molangcompiler.api.object.ImmutableMolangObject;
import gg.moonflower.molangcompiler.api.object.MolangObject;
import gg.moonflower.molangcompiler.core.object.MolangVariableStorage;

import java.util.*;

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
        this.loadLibrary("context", query, "c"); // This is static accesses
        this.loadLibrary("query", query, "q"); // This is static accesses
        this.loadLibrary("global", global); // This is parameter access
        this.loadLibrary("variable", variable, "v"); // This can be accessed by Java code
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
    public void loadLibrary(String name, MolangObject object, String... aliases) {
        this.objects.put(name.toLowerCase(Locale.ROOT), object);
        for (String alias : aliases) {
            this.objects.put(alias.toLowerCase(Locale.ROOT), object);
            this.aliases.add(alias);
        }
    }

    @Override
    public void loadAlias(String name, String first, String... aliases) {
        if (!this.objects.containsKey(name)) {
            throw new IllegalArgumentException("Invalid MoLang library: " + name);
        }

        this.aliases.add(first);
        if (aliases.length > 0) {
            this.aliases.addAll(Arrays.asList(aliases));
        }
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
        MolangObject object = this.objects.get(name);
        if (object != null) {
            return object;
        }
        throw new MolangRuntimeException("Unknown MoLang object: " + name);
    }

    @Override
    public float getParameter(int parameter) throws MolangRuntimeException {
        if (parameter < 0 || parameter >= this.parameters.size()) {
            throw new MolangRuntimeException("No parameter loaded in slot " + parameter);
        }
        return this.parameters.get(parameter);
    }

    @Override
    public int getParameters() {
        return this.parameters.size();
    }

    @Override
    public void setThisValue(float thisValue) {
        this.thisValue = thisValue;
    }

    private MolangVariableStorage getStorage(String name) {
        MolangObject object = this.objects.get(name);
        if (object == null) {
            throw new IllegalStateException("Missing " + name);
        }
        if (object instanceof ImmutableMolangObject immutableObject) {
            object = immutableObject.parent();
        }
        if (!(object instanceof MolangVariableStorage variableStorage)) {
            throw new IllegalStateException("Expected " + name + " to be " + MolangVariableStorage.class.getName() + ", was " + object.getClass().getName());
        }
        return variableStorage;
    }

    /**
     * Creates a new builder for editing the current runtime.
     * {@link MolangEnvironmentBuilder#create()} will return this runtime.
     *
     * @return A builder for modifying the current runtime
     * @since 3.0.0
     */
    public MolangEnvironmentBuilder<MolangRuntime> edit() {
        MolangVariableStorage query = this.getStorage("query");
        MolangVariableStorage global = this.getStorage("global");
        MolangVariableStorage variable = this.getStorage("variable");
        return new EditBuilder(this, query, global, variable);
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
    public static class Builder implements MolangEnvironmentBuilder<MolangRuntime> {

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

        @Override
        public Builder loadLibrary(String name, MolangObject object) {
            this.libraries.put(name, object);
            return this;
        }

        @Override
        public MolangEnvironmentBuilder<MolangRuntime> unloadLibrary(String name) {
            this.libraries.remove(name);
            return this;
        }

        @Override
        public Builder setQuery(String name, MolangExpression value) {
            try {
                this.query.set(name, value);
            } catch (MolangRuntimeException e) {
                throw new IllegalStateException(e);
            }
            return this;
        }

        @Override
        public Builder setGlobal(String name, MolangExpression value) {
            try {
                this.global.set(name, value);
            } catch (MolangRuntimeException e) {
                throw new RuntimeException(e);
            }
            return this;
        }

        @Override
        public Builder setVariable(String name, MolangExpression value) {
            try {
                this.variable.set(name, value);
            } catch (MolangRuntimeException e) {
                throw new IllegalStateException(e);
            }
            return this;
        }

        @Override
        public Builder removeQuery(String name) {
            try {
                this.query.remove(name);
            } catch (MolangRuntimeException e) {
                throw new IllegalStateException(e);
            }
            return this;
        }

        @Override
        public Builder removeGlobal(String name) {
            try {
                this.global.remove(name);
            } catch (MolangRuntimeException e) {
                throw new IllegalStateException(e);
            }
            return this;
        }

        @Override
        public Builder removeVariable(String name) {
            try {
                this.variable.remove(name);
            } catch (MolangRuntimeException e) {
                throw new IllegalStateException(e);
            }
            return this;
        }

        @Override
        public Builder clearLibraries() {
            this.libraries.clear();
            return this;
        }

        @Override
        public Builder clearQuery() {
            this.query.clear();
            return this;
        }

        @Override
        public Builder clearGlobal() {
            this.global.clear();
            return this;
        }

        @Override
        public Builder clearVariable() {
            this.variable.clear();
            return this;
        }

        @Override
        public MolangRuntime create() {
            return new MolangRuntime(new ImmutableMolangObject(this.query), new ImmutableMolangObject(this.global), this.variable, this.libraries);
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

    private record EditBuilder(MolangRuntime runtime,
                               MolangVariableStorage query,
                               MolangVariableStorage global,
                               MolangVariableStorage variable) implements MolangEnvironmentBuilder<MolangRuntime> {

        @Override
        public MolangEnvironmentBuilder<MolangRuntime> loadLibrary(String name, MolangObject object) {
            this.runtime.loadLibrary(name, object);
            return this;
        }

        @Override
        public MolangEnvironmentBuilder<MolangRuntime> unloadLibrary(String name) {
            MolangObject removed = this.runtime.objects.get(name);
            if (removed == this.query || removed == this.global || removed == this.variable) {
                throw new IllegalStateException("Cannot remove query, global, or variable");
            }

            this.runtime.objects.remove(name);
            return this;
        }

        private MolangEnvironmentBuilder<MolangRuntime> set(MolangObject object, String name, MolangExpression value) {
            try {
                object.set(name, value);
            } catch (MolangRuntimeException e) {
                throw new IllegalStateException(e);
            }
            return this;
        }

        private MolangEnvironmentBuilder<MolangRuntime> remove(MolangObject object, String name) {
            try {
                object.remove(name);
            } catch (MolangRuntimeException e) {
                throw new IllegalStateException(e);
            }
            return this;
        }

        @Override
        public MolangEnvironmentBuilder<MolangRuntime> setQuery(String name, MolangExpression value) {
            return this.set(this.query, name, value);
        }

        @Override
        public MolangEnvironmentBuilder<MolangRuntime> setGlobal(String name, MolangExpression value) {
            return this.set(this.global, name, value);
        }

        @Override
        public MolangEnvironmentBuilder<MolangRuntime> setVariable(String name, MolangExpression value) {
            return this.set(this.variable, name, value);
        }

        @Override
        public MolangEnvironmentBuilder<MolangRuntime> removeQuery(String name) {
            return this.remove(this.query, name);
        }

        @Override
        public MolangEnvironmentBuilder<MolangRuntime> removeGlobal(String name) {
            return this.remove(this.global, name);
        }

        @Override
        public MolangEnvironmentBuilder<MolangRuntime> removeVariable(String name) {
            return this.remove(this.variable, name);
        }

        @Override
        public MolangEnvironmentBuilder<MolangRuntime> clearLibraries() {
            this.runtime.objects.values().retainAll(List.of(this.query, this.global, this.variable));
            return this;
        }

        @Override
        public MolangEnvironmentBuilder<MolangRuntime> clearQuery() {
            this.query.clear();
            return this;
        }

        @Override
        public MolangEnvironmentBuilder<MolangRuntime> clearGlobal() {
            this.global.clear();
            return this;
        }

        @Override
        public MolangEnvironmentBuilder<MolangRuntime> clearVariable() {
            this.variable.clear();
            return this;
        }

        @Override
        public MolangRuntime create() {
            return this.runtime;
        }
    }
}
