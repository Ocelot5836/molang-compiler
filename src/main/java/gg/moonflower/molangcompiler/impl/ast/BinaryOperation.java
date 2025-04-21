package gg.moonflower.molangcompiler.impl.ast;

import org.jetbrains.annotations.ApiStatus;

import java.util.Objects;

/**
 * @author Buddy
 */
@ApiStatus.Internal
public enum BinaryOperation {
    ADD("+"),
    SUBTRACT("-"),
    MULTIPLY("*"),
    DIVIDE("/"),
    AND("&&"),
    OR("||"),
    EQUALS("=="),
    NOT_EQUALS("!="),
    LESS_EQUALS("<="),
    LESS("<"),
    GREATER_EQUALS(">="),
    GREATER(">"),
    NULL_COALESCING("??");

    private final String value;

    BinaryOperation(String value) {
        this.value = Objects.requireNonNull(value, "value");
    }

    public String getValue() {
        return this.value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}