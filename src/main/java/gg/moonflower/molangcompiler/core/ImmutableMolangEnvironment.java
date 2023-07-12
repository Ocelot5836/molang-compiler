package gg.moonflower.molangcompiler.core;

import gg.moonflower.molangcompiler.api.MolangEnvironment;
import gg.moonflower.molangcompiler.api.MolangEnvironmentBuilder;
import gg.moonflower.molangcompiler.api.exception.MolangRuntimeException;
import gg.moonflower.molangcompiler.api.object.MolangObject;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collection;

/**
 * @author Ocelot
 */
@ApiStatus.Internal
public record ImmutableMolangEnvironment(MolangEnvironment environment) implements MolangEnvironment {

    @Override
    public void loadLibrary(String name, MolangObject object, String... aliases) {
        this.environment.loadLibrary(name, object, aliases);
    }

    @Override
    public void loadAlias(String name, String first, String... aliases) throws IllegalArgumentException {
        this.environment.loadAlias(name, first, aliases);
    }

    @Override
    public void loadParameter(float value) throws MolangRuntimeException {
        this.environment.loadParameter(value);
    }

    @Override
    public void clearParameters() {
        this.environment.clearParameters();
    }

    @Override
    public float getThis() {
        return this.environment.getThis();
    }

    @Override
    public MolangObject get(String name) throws MolangRuntimeException {
        return this.environment.get(name);
    }

    @Override
    public float getParameter(int parameter) throws MolangRuntimeException {
        return this.environment.getParameter(parameter);
    }

    @Override
    public int getParameters() {
        return this.environment.getParameters();
    }

    @Override
    public Collection<String> getObjects() {
        return this.environment.getObjects();
    }

    @Override
    public void setThisValue(float thisValue) {
        this.environment.setThisValue(thisValue);
    }

    @Override
    public boolean canEdit() {
        return false;
    }

    @Override
    public MolangEnvironmentBuilder<? extends MolangEnvironment> edit() throws IllegalStateException {
        throw new IllegalStateException("Immutable MoLang environments cannot be edited");
    }
}
