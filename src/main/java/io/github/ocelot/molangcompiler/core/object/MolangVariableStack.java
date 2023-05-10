package io.github.ocelot.molangcompiler.core.object;

import io.github.ocelot.molangcompiler.api.MolangExpression;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * @author Ocelot
 */
@ApiStatus.Internal
public class MolangVariableStack extends MolangVariableStorage {

    private final Stack<Map<String, MolangExpression>> stack;

    public MolangVariableStack(boolean allowMethods) {
        super(allowMethods);
        this.stack = new Stack<>();
        this.stack.push(new HashMap<>());
    }

    @Override
    protected Map<String, MolangExpression> getStorage() {
        return this.stack.peek();
    }

    public void push() {
        this.stack.push(new HashMap<>(this.stack.peek()));
    }

    public void pop() {
        if (this.stack.size() > 1) {
            this.stack.pop();
        }
    }
}
