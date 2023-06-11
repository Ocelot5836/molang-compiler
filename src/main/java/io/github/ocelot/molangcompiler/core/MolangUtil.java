package io.github.ocelot.molangcompiler.core;

import io.github.ocelot.molangcompiler.api.MolangEnvironment;
import io.github.ocelot.molangcompiler.api.MolangExpression;
import io.github.ocelot.molangcompiler.api.bridge.MolangVariable;
import io.github.ocelot.molangcompiler.api.exception.MolangException;
import io.github.ocelot.molangcompiler.api.exception.MolangRuntimeException;
import io.github.ocelot.molangcompiler.api.object.MolangObject;
import io.github.ocelot.molangcompiler.core.node.MolangConstantNode;
import io.github.ocelot.molangcompiler.core.node.MolangVariableNode;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class MolangUtil {

    public static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    public static float dieRoll(int num, float low, float high) {
        float sum = 0;
        for (int i = 0; i < num; i++) {
            sum += low + Math.random() * (high - low);
        }
        return sum;
    }

    public static float dieRollInt(int num, int low, int high) {
        int sum = 0;
        for (int i = 0; i < num; i++) {
            sum += random(low, high);
        }
        return sum;
    }

    public static float hermiteBlend(float value) {
        return 3 * value * value - 2 * value * value * value;
    }

    public static float lerp(float start, float end, float pct) {
        return start + (end - start) * pct;
    }

    public static float lerpRotate(float start, float end, float pct) {
        return start + wrapDegrees(end - start) * pct;
    }

    public static float wrapDegrees(float angle) {
        float wrapped = angle % 360.0F;
        if (wrapped >= 180.0F) {
            wrapped -= 360.0F;
        }

        if (wrapped < -180.0F) {
            wrapped += 360.0F;
        }

        return wrapped;
    }

    public static float random(float low, float high) {
        return (float) (low + Math.random() * (high - low));
    }

    public static MolangExpression getFunction(MolangObject object, String name, String fullKey) throws MolangException {
        if (object.has(fullKey)) {
            return object.get(fullKey);
        } else if (object.has(name)) {
            return object.get(name);
        } else {
            throw new MolangException("Unknown function: " + object + "." + fullKey + "()");
        }
    }

    public static void setValue(MolangEnvironment environment, String object, String name, float value) throws MolangRuntimeException {
        MolangObject obj = environment.get(object);
        if (!obj.has(name)) {
            obj.set(name, new MolangVariableNode(MolangVariable.create(value)));
            return;
        }

        MolangExpression old = obj.get(name);
        if (old instanceof MolangVariable variable) {
            variable.setValue(value);
        } else {
            obj.set(name, new MolangConstantNode(value));
        }
    }
}
