package io.github.ocelot.molangcompiler.core.object;

import io.github.ocelot.molangcompiler.api.MolangEnvironment;
import io.github.ocelot.molangcompiler.api.MolangExpression;
import io.github.ocelot.molangcompiler.api.bridge.MolangJavaFunction;
import io.github.ocelot.molangcompiler.api.exception.MolangException;
import io.github.ocelot.molangcompiler.core.MolangJavaFunctionContext;
import org.jetbrains.annotations.ApiStatus;

/**
 * @author Ocelot
 */
@ApiStatus.Internal
public class MolangFunction implements MolangExpression {

    private final int params;
    private final MolangJavaFunction consumer;

    public MolangFunction(int params, MolangJavaFunction consumer) {
        this.params = params;
        this.consumer = consumer;
    }

    @Override
    public float resolve(MolangEnvironment environment) throws MolangException {
        MolangExpression[] parameters = new MolangExpression[this.params];
        for (int i = 0; i < parameters.length; i++) {
            if (!environment.hasParameter(i))
                throw new IllegalStateException("Function requires " + parameters.length + " parameters");
            parameters[i] = environment.getParameter(i);
        }
        return this.consumer.resolve(new MolangJavaFunctionContext(environment, parameters));
    }
}
