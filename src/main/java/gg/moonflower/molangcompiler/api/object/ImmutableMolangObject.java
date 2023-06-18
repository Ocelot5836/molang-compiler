package gg.moonflower.molangcompiler.api.object;

import gg.moonflower.molangcompiler.api.MolangExpression;
import gg.moonflower.molangcompiler.api.exception.MolangRuntimeException;

import java.util.Collection;

/**
 * A {@link MolangObject} that cannot have any values modified.
 *
 * @author Ocelot
 * @since 1.0.0
 */
public record ImmutableMolangObject(MolangObject parent) implements MolangObject {

    @Override
    public void set(String name, MolangExpression value) throws MolangRuntimeException {
        throw new MolangRuntimeException("Cannot set values on an immutable object");
    }

    @Override
    public void remove(String name) throws MolangRuntimeException {
        throw new MolangRuntimeException("Cannot set values on an immutable object");
    }

    @Override
    public MolangExpression get(String name) throws MolangRuntimeException {
        return this.parent.get(name);
    }

    @Override
    public boolean has(String name) {
        return this.parent.has(name);
    }

    @Override
    public Collection<String> getKeys() {
        return this.parent.getKeys();
    }

    @Override
    public String toString() {
        return this.parent.toString();
    }
}
