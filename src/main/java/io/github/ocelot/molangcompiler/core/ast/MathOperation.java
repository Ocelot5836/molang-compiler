package io.github.ocelot.molangcompiler.core.ast;

import java.util.Locale;

/**
 * @author Ocelot
 */
public enum MathOperation {
    ABS,
    ACOS,
    ASIN,
    ATAN,
    ATAN2,
    CEIL,
    CLAMP,
    COS,
    SIN,
    DIE_ROLL(false),
    DIE_ROLL_INTEGER(false),
    EXP,
    FLOOR,
    HERMITE_BLEND,
    LERP,
    LERPROTATE,
    LN,
    MAX,
    MIN,
    MIN_ANGLE,
    MOD,
    PI,
    POW,
    RANDOM(false),
    RANDOM_INTEGER(false),
    ROUND,
    SQRT,
    TRUNC;

    private final String name;
    private final boolean deterministic;

    MathOperation() {
        this(true);
    }

    MathOperation(boolean deterministic) {
        this.deterministic = deterministic;
        this.name = this.name().toLowerCase(Locale.ROOT);
    }

    public String getName() {
        return this.name;
    }

    public boolean isDeterministic() {
        return this.deterministic;
    }

    @Override
    public String toString() {
        return this.name;
    }
}