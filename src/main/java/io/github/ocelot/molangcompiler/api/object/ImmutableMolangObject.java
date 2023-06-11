package io.github.ocelot.molangcompiler.api.object;

import io.github.ocelot.molangcompiler.api.MolangExpression;
import io.github.ocelot.molangcompiler.api.exception.MolangRuntimeException;

/**
 * A {@link MolangObject} that cannot have any values modified.
 *
 * @author Ocelot
 * @since 1.0.0
 */
public class ImmutableMolangObject implements MolangObject {

    private final MolangObject parent;

    public ImmutableMolangObject(MolangObject parent) {
        this.parent = parent;
    }

    @Override
    public void set(String name, MolangExpression value) throws MolangRuntimeException {
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
    public String toString() {
        return String.valueOf(this.parent);
    }
}
