package gg.moonflower.molangcompiler.core.ast;

import gg.moonflower.molangcompiler.api.exception.MolangException;
import gg.moonflower.molangcompiler.core.compiler.MolangBytecodeEnvironment;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Label;
import org.objectweb.asm.tree.MethodNode;

/**
 * Represents a local scope with independent variables.
 *
 * @param node The node within this scope
 * @author Ocelot
 */
@ApiStatus.Internal
public record ScopeNode(Node node) implements Node {

    @Override
    public String toString() {
        return "{" + this.node + "}";
    }

    @Override
    public boolean isConstant() {
        return this.node.isConstant();
    }

    @Override
    public boolean hasValue() {
        return this.node.hasValue();
    }

    @Override
    public float evaluate(MolangBytecodeEnvironment environment) throws MolangException {
        return this.node.evaluate(environment);
    }

    @Override
    public void writeBytecode(MethodNode method, MolangBytecodeEnvironment environment, @Nullable Label breakLabel, @Nullable Label continueLabel) throws MolangException {
        MolangBytecodeEnvironment scopeEnvironment = new MolangBytecodeEnvironment(environment);
        this.node.writeBytecode(method, scopeEnvironment, breakLabel, continueLabel);
        scopeEnvironment.writeModifiedVariables(method);
    }
}
