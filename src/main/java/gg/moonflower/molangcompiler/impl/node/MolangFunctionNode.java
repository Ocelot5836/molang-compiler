package gg.moonflower.molangcompiler.impl.node;

import gg.moonflower.molangcompiler.api.MolangEnvironment;
import gg.moonflower.molangcompiler.api.MolangExpression;
import gg.moonflower.molangcompiler.api.bridge.MolangJavaFunction;
import gg.moonflower.molangcompiler.api.exception.MolangRuntimeException;
import org.jetbrains.annotations.ApiStatus;

/**
 * @author Ocelot
 */
@ApiStatus.Internal
public class MolangFunctionNode implements MolangExpression {

    private final int params;
    private final MolangJavaFunction consumer;

    public MolangFunctionNode(int params, MolangJavaFunction consumer) {
        this.params = params;
        this.consumer = consumer;
    }

    @Override
    public float get(MolangEnvironment environment) throws MolangRuntimeException {
        float[] parameters;
        if (this.params < 0) {
            parameters = new float[environment.getParameters()];
            for (int i = 0; i < parameters.length; i++) {
                parameters[i] = environment.getParameter(i);
            }
        } else {
            parameters = new float[this.params];
            for (int i = 0; i < parameters.length; i++) {
                if (!environment.hasParameter(i)) {
                    throw new MolangRuntimeException("Function requires " + parameters.length + " parameters");
                }
                parameters[i] = environment.getParameter(i);
            }
        }
        return this.consumer.resolve(new MolangJavaFunction.Context(parameters));
    }
}
