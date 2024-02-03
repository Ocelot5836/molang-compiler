package gg.moonflower.molangcompiler.core.ast;

import gg.moonflower.molangcompiler.api.exception.MolangException;
import gg.moonflower.molangcompiler.core.MolangUtil;
import gg.moonflower.molangcompiler.core.compiler.BytecodeCompiler;
import gg.moonflower.molangcompiler.core.compiler.MolangBytecodeEnvironment;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Runs a math function directly from Java if possible.
 *
 * @param function  The function to run
 * @param arguments The parameters to pass into the function
 * @author Ocelot, Buddy
 */
@ApiStatus.Internal
public record MathNode(MathOperation function, Node... arguments) implements Node {

    private static final float RADIANS_TO_DEGREES = (float) (180 / Math.PI);
    private static final float DEGREES_TO_RADIANS = (float) (Math.PI / 180);

    @Override
    public String toString() {
        if (this.function.getParameters() == 0) {
            return "math." + this.function.getName();
        }
        return "math." + this.function.getName() + "(" + Arrays.stream(this.arguments).map(Node::toString).collect(Collectors.joining(", ")) + ")";
    }

    @Override
    public boolean isConstant() {
        if (!this.function.isDeterministic()) {
            return false;
        }

        for (Node parameter : this.arguments) {
            if (!parameter.isConstant()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean hasValue() {
        return true;
    }

    @Override
    public float evaluate(MolangBytecodeEnvironment environment) throws MolangException {
        float[] values = new float[this.arguments.length];
        for (int i = 0; i < values.length; i++) {
            values[i] = this.arguments[i].evaluate(environment);
        }

        return switch (this.function) {
            case ABS -> Math.abs(values[0]);
            case ACOS -> RADIANS_TO_DEGREES * (float) Math.acos(values[0]);
            case ASIN -> RADIANS_TO_DEGREES * (float) Math.asin(values[0]);
            case ATAN -> RADIANS_TO_DEGREES * (float) Math.atan(values[0]);
            case ATAN2 -> RADIANS_TO_DEGREES * (float) Math.atan2(values[0], values[1]);
            case CEIL -> (float) Math.ceil(values[0]);
            case CLAMP -> MolangUtil.clamp(values[0], values[1], values[2]);
            case COS -> (float) Math.cos(DEGREES_TO_RADIANS * values[0]);
            case SIN -> (float) Math.sin(DEGREES_TO_RADIANS * values[0]);
            case EXP -> (float) Math.exp(values[0]);
            case FLOOR -> (float) Math.floor(values[0]);
            case HERMITE_BLEND -> MolangUtil.hermiteBlend(values[0]);
            case LERP -> MolangUtil.lerp(values[0], values[1], values[2]);
            case LERPROTATE -> MolangUtil.lerpRotate(values[0], values[1], values[2]);
            case LN -> (float) Math.log(values[0]);
            case MAX -> Math.max(values[0], values[1]);
            case MIN -> Math.min(values[0], values[1]);
            case MIN_ANGLE -> MolangUtil.wrapDegrees(values[0]);
            case MOD -> values[0] % values[1];
            case PI -> (float) Math.PI;
            case POW -> (float) Math.pow(values[0], values[1]);
            case ROUND -> Math.round(values[0]);
            case SQRT -> (float) Math.sqrt(values[0]);
            case TRUNC -> (int) values[0];
            case SIGN -> Math.signum(values[0]);
            case TRIANGLE_WAVE -> MolangUtil.triangleWave(values[0], values[1]);
            default -> throw new MolangException("Unexpected value: " + this.function);
        };
    }

    @Override
    public void writeBytecode(MethodNode method, MolangBytecodeEnvironment env, @Nullable Label breakLabel, @Nullable Label continueLabel) throws MolangException {
        switch (this.function) {
            // Single-argument Float
            case ABS -> {
                this.arguments[0].writeBytecode(method, env, breakLabel, continueLabel);
                method.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Math", this.function.getName(), "(F)F", false);
            }
            // Double-argument Float
            case MAX, MIN -> {
                this.arguments[0].writeBytecode(method, env, breakLabel, continueLabel);
                this.arguments[1].writeBytecode(method, env, breakLabel, continueLabel);
                method.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Math", this.function.getName(), "(FF)F", false);
            }
            // Convert to degrees
            case ACOS, ASIN, ATAN -> {
                this.arguments[0].writeBytecode(method, env, breakLabel, continueLabel);
                method.visitInsn(Opcodes.F2D);
                method.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Math", this.function.getName(), "(D)D", false);
                method.visitInsn(Opcodes.D2F);
                BytecodeCompiler.writeFloatConst(method, RADIANS_TO_DEGREES);
                method.visitInsn(Opcodes.FMUL);
            }
            // Single-argument Double
            case CEIL, EXP, FLOOR, LN, SQRT -> {
                this.arguments[0].writeBytecode(method, env, breakLabel, continueLabel);
                method.visitInsn(Opcodes.F2D);
                method.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Math", this.function.getName(), "(D)D", false);
                method.visitInsn(Opcodes.D2F);
            }
            // Convert to radians
            case COS, SIN -> {
                this.arguments[0].writeBytecode(method, env, breakLabel, continueLabel);
                BytecodeCompiler.writeFloatConst(method, DEGREES_TO_RADIANS);
                method.visitInsn(Opcodes.FMUL);
                method.visitInsn(Opcodes.F2D);
                method.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Math", this.function.getName(), "(D)D", false);
                method.visitInsn(Opcodes.D2F);
            }
            // Double-argument Double
            case POW -> {
                this.arguments[0].writeBytecode(method, env, breakLabel, continueLabel);
                method.visitInsn(Opcodes.F2D);
                this.arguments[1].writeBytecode(method, env, breakLabel, continueLabel);
                method.visitInsn(Opcodes.F2D);
                method.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Math", this.function.getName(), "(DD)D", false);
                method.visitInsn(Opcodes.D2F);
            }
            // Convert to degrees
            case ATAN2 -> {
                this.arguments[0].writeBytecode(method, env, breakLabel, continueLabel);
                method.visitInsn(Opcodes.F2D);
                this.arguments[1].writeBytecode(method, env, breakLabel, continueLabel);
                method.visitInsn(Opcodes.F2D);
                method.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Math", this.function.getName(), "(DD)D", false);
                method.visitInsn(Opcodes.D2F);
                BytecodeCompiler.writeFloatConst(method, RADIANS_TO_DEGREES);
                method.visitInsn(Opcodes.FMUL);
            }
            // Single-argument Float->Int
            case ROUND -> {
                this.arguments[0].writeBytecode(method, env, breakLabel, continueLabel);
                method.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Math", this.function.getName(), "(F)I", false);
                method.visitInsn(Opcodes.I2F);
            }
            // Operations
            case MOD -> {
                this.arguments[0].writeBytecode(method, env, breakLabel, continueLabel);
                this.arguments[1].writeBytecode(method, env, breakLabel, continueLabel);
                method.visitInsn(Opcodes.FREM);
            }
            case PI -> BytecodeCompiler.writeFloatConst(method, (float) Math.PI);
            case TRUNC -> {
                this.arguments[0].writeBytecode(method, env, breakLabel, continueLabel);
                method.visitInsn(Opcodes.F2I);
                method.visitInsn(Opcodes.I2F);
            }
            // Custom
            case CLAMP -> {
                this.arguments[0].writeBytecode(method, env, breakLabel, continueLabel);
                this.arguments[1].writeBytecode(method, env, breakLabel, continueLabel);
                this.arguments[2].writeBytecode(method, env, breakLabel, continueLabel);
                method.visitMethodInsn(Opcodes.INVOKESTATIC, "gg/moonflower/molangcompiler/core/MolangUtil", "clamp", "(FFF)F", false);
            }
            case DIE_ROLL -> {
                this.arguments[0].writeBytecode(method, env, breakLabel, continueLabel);
                method.visitInsn(Opcodes.F2I);
                this.arguments[1].writeBytecode(method, env, breakLabel, continueLabel);
                this.arguments[2].writeBytecode(method, env, breakLabel, continueLabel);
                method.visitMethodInsn(Opcodes.INVOKESTATIC, "gg/moonflower/molangcompiler/core/MolangUtil", "dieRoll", "(IFF)F", false);
            }
            case DIE_ROLL_INTEGER -> {
                this.arguments[0].writeBytecode(method, env, breakLabel, continueLabel);
                method.visitInsn(Opcodes.F2I);
                this.arguments[1].writeBytecode(method, env, breakLabel, continueLabel);
                method.visitInsn(Opcodes.F2I);
                method.visitInsn(Opcodes.I2F);
                this.arguments[2].writeBytecode(method, env, breakLabel, continueLabel);
                method.visitInsn(Opcodes.F2I);
                method.visitInsn(Opcodes.I2F);
                method.visitMethodInsn(Opcodes.INVOKESTATIC, "gg/moonflower/molangcompiler/core/MolangUtil", "dieRoll", "(IFF)F", false);
                method.visitInsn(Opcodes.F2I);
                method.visitInsn(Opcodes.I2F);
            }
            case HERMITE_BLEND -> {
                this.arguments[0].writeBytecode(method, env, breakLabel, continueLabel);
                method.visitMethodInsn(Opcodes.INVOKESTATIC, "gg/moonflower/molangcompiler/core/MolangUtil", "hermiteBlend", "(F)F", false);
            }
            case LERP -> {
                this.arguments[0].writeBytecode(method, env, breakLabel, continueLabel);
                this.arguments[1].writeBytecode(method, env, breakLabel, continueLabel);
                this.arguments[2].writeBytecode(method, env, breakLabel, continueLabel);
                method.visitMethodInsn(Opcodes.INVOKESTATIC, "gg/moonflower/molangcompiler/core/MolangUtil", "lerp", "(FFF)F", false);
            }
            case LERPROTATE -> {
                this.arguments[0].writeBytecode(method, env, breakLabel, continueLabel);
                this.arguments[1].writeBytecode(method, env, breakLabel, continueLabel);
                this.arguments[2].writeBytecode(method, env, breakLabel, continueLabel);
                method.visitMethodInsn(Opcodes.INVOKESTATIC, "gg/moonflower/molangcompiler/core/MolangUtil", "lerpRotate", "(FFF)F", false);
            }
            case MIN_ANGLE -> {
                this.arguments[0].writeBytecode(method, env, breakLabel, continueLabel);
                method.visitMethodInsn(Opcodes.INVOKESTATIC, "gg/moonflower/molangcompiler/core/MolangUtil", "wrapDegrees", "(F)F", false);
            }
            case RANDOM -> {
                this.arguments[0].writeBytecode(method, env, breakLabel, continueLabel);
                this.arguments[1].writeBytecode(method, env, breakLabel, continueLabel);
                method.visitMethodInsn(Opcodes.INVOKESTATIC, "gg/moonflower/molangcompiler/core/MolangUtil", "random", "(FF)F", false);
            }
            case RANDOM_INTEGER -> {
                this.arguments[0].writeBytecode(method, env, breakLabel, continueLabel);
                method.visitInsn(Opcodes.F2I);
                method.visitInsn(Opcodes.I2F);
                this.arguments[1].writeBytecode(method, env, breakLabel, continueLabel);
                method.visitInsn(Opcodes.F2I);
                method.visitInsn(Opcodes.I2F);
                method.visitMethodInsn(Opcodes.INVOKESTATIC, "gg/moonflower/molangcompiler/core/MolangUtil", "random", "(FF)F", false);
                method.visitInsn(Opcodes.F2I);
                method.visitInsn(Opcodes.I2F);
            }
            case SIGN -> {
                this.arguments[0].writeBytecode(method, env, breakLabel, continueLabel);
                method.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Math", "signum", "(F)F", false);
            }
            case TRIANGLE_WAVE -> {
                this.arguments[0].writeBytecode(method, env, breakLabel, continueLabel);
                this.arguments[1].writeBytecode(method, env, breakLabel, continueLabel);
                method.visitMethodInsn(Opcodes.INVOKESTATIC, "gg/moonflower/molangcompiler/core/MolangUtil", "triangleWave", "(FF)F", false);
            }
        }
    }
}