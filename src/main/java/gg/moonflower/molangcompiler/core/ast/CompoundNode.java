package gg.moonflower.molangcompiler.core.ast;

import gg.moonflower.molangcompiler.api.exception.MolangException;
import gg.moonflower.molangcompiler.core.compiler.MolangBytecodeEnvironment;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Label;
import org.objectweb.asm.tree.MethodNode;

/**
 * Inserts multiple nodes in order.
 *
 * @param nodes The nodes to insert in order
 * @author Ocelot
 */
@ApiStatus.Internal
public record CompoundNode(Node... nodes) implements Node {

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (Node node : this.nodes) {
            builder.append(node).append(";\n");
        }
        return builder.toString();
    }

    @Override
    public boolean isConstant() {
        return this.nodes.length == 1 && this.nodes[0].isConstant();
    }

    @Override
    public boolean hasValue() {
        return this.nodes.length > 0 && this.nodes[this.nodes.length - 1].hasValue();
    }

    @Override
    public float evaluate(MolangBytecodeEnvironment environment) throws MolangException {
        return this.nodes[0].evaluate(environment);
    }

    @Override
    public void writeBytecode(MethodNode method, MolangBytecodeEnvironment environment, @Nullable Label breakLabel, @Nullable Label continueLabel) throws MolangException {
        for (Node node : this.nodes) {
            node.writeBytecode(method, environment, breakLabel, continueLabel);
        }
    }
}
