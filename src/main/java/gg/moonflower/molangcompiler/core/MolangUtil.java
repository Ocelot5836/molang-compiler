package gg.moonflower.molangcompiler.core;

import gg.moonflower.molangcompiler.api.MolangExpression;
import gg.moonflower.molangcompiler.api.bridge.MolangVariable;
import gg.moonflower.molangcompiler.api.exception.MolangException;
import gg.moonflower.molangcompiler.api.exception.MolangRuntimeException;
import gg.moonflower.molangcompiler.api.object.MolangObject;
import gg.moonflower.molangcompiler.core.node.MolangConstantNode;
import gg.moonflower.molangcompiler.core.node.MolangVariableNode;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
@SuppressWarnings("unused") // Methods are referenced in ASM
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

    public static void setValue(MolangObject object, String name, float value) throws MolangRuntimeException {
        if (!object.has(name)) {
            object.set(name, new MolangVariableNode(MolangVariable.create(value)));
            return;
        }

        MolangExpression old = object.get(name);
        if (old instanceof MolangVariable variable) {
            variable.setValue(value);
        } else {
            object.set(name, new MolangConstantNode(value));
        }
    }
}
