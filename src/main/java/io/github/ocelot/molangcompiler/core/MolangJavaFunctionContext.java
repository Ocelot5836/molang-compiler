package io.github.ocelot.molangcompiler.core;

import io.github.ocelot.molangcompiler.api.bridge.MolangJavaFunction;
import io.github.ocelot.molangcompiler.api.exception.MolangException;
import io.github.ocelot.molangcompiler.api.exception.MolangRuntimeException;
import org.jetbrains.annotations.ApiStatus;

/**
 * @author Ocelot
 */
@ApiStatus.Internal
public class MolangJavaFunctionContext implements MolangJavaFunction.Context {

    private final float[] parameters;

    public MolangJavaFunctionContext(float[] parameters) {
        this.parameters = parameters;
    }

    @Override
    public float get(int parameter) throws MolangRuntimeException {
        if (parameter < 0 || parameter >= this.parameters.length) {
            throw new MolangRuntimeException("Invalid parameter: " + parameter);
        }
        return this.parameters[parameter];
    }

    @Override
    public int getParameters() {
        return this.parameters.length;
    }
}
