package io.github.ocelot.molangcompiler.core.compiler;

import io.github.ocelot.molangcompiler.api.exception.MolangSyntaxException;
import org.jetbrains.annotations.ApiStatus;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Ocelot
 */
@ApiStatus.Internal
public record MolangBytecodeEnvironment(Map<String, Integer> variables,
                                        Set<String> modifiedVariables,
                                        boolean optimize) {

    public MolangBytecodeEnvironment(MolangBytecodeEnvironment environment) {
        this(new HashMap<>(environment.variables), new HashSet<>(), environment.optimize);
    }

    public MolangBytecodeEnvironment(int flags) {
        this(new HashMap<>(), new HashSet<>(), (flags & BytecodeCompiler.FLAG_OPTIMIZE) > 0);
    }

    /**
     * Fully resets the environment.
     */
    public void reset() {
        this.variables.clear();
        this.modifiedVariables.clear();
    }

    /**
     * Allocates a local variable for temporary use. This can be re-used as many times as necessary
     *
     * @param index The index of the temporary variable
     * @return The index of the local variable
     */
    public int getTempVariableIndex(int index) {
        return this.allocateVariable("temp" + index);
    }

    /**
     * Loads the specified variable into a local variable if necessary.
     *
     * @param method The method to insert the local into if not present
     * @param object The object to get the variable from
     * @param name   The name of the variable to get
     * @return The index of the local variable the value is stored in
     */
    public int loadVariable(MethodNode method, String object, String name) {
        String key = object + "." + name;
        Integer index = this.variables.get(key);
        if (index != null) {
            return index;
        }

        // Get variable
        int objectIndex = this.getObjectIndex(method, object);
        method.visitVarInsn(Opcodes.ALOAD, objectIndex);
        method.visitLdcInsn(name);
        method.visitMethodInsn(
                Opcodes.INVOKEINTERFACE,
                "io/github/ocelot/molangcompiler/api/object/MolangObject",
                "get",
                "(Ljava/lang/String;)Lio/github/ocelot/molangcompiler/api/MolangExpression;",
                true
        );

        // Resolve value
        method.visitVarInsn(Opcodes.ALOAD, BytecodeCompiler.RUNTIME_INDEX);
        method.visitMethodInsn(
                Opcodes.INVOKEINTERFACE,
                "io/github/ocelot/molangcompiler/api/MolangExpression",
                "resolve",
                "(Lio/github/ocelot/molangcompiler/api/MolangEnvironment;)F",
                true
        );

        // Store result
        index = this.allocateVariable(key);
        method.visitVarInsn(Opcodes.FSTORE, index);
        return index;
    }

    /**
     * Allocates a space for a new variable, but doesn't initialize it.
     *
     * @param name The full name of the variable, including any objects it may be in
     * @return The index the variable can be loaded into
     */
    public int allocateVariable(String name) {
        Integer index = this.variables.get(name);
        if (index != null) {
            return index;
        }

        // Calculate variable offset
        index = this.variables.size() + BytecodeCompiler.VARIABLE_START;
        this.variables.put(name, index);
        return index;
    }

    /**
     * Retrieves the local index for the specified object, loading it if necessary.
     *
     * @param method The method to insert the local variable into if required
     * @param object The name of the object to query
     */
    public int getObjectIndex(MethodNode method, String object) {
        Integer index = this.variables.get(object);
        if (index != null) {
            return index;
        }

        index = this.allocateVariable(object);

        method.visitVarInsn(Opcodes.ALOAD, BytecodeCompiler.RUNTIME_INDEX);
        method.visitLdcInsn(object);
        method.visitMethodInsn(
                Opcodes.INVOKEINTERFACE,
                "io/github/ocelot/molangcompiler/api/MolangEnvironment",
                "get",
                "(Ljava/lang/String;)Lio/github/ocelot/molangcompiler/api/object/MolangObject;",
                true);
        method.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                "java/util/Objects",
                "requireNonNull",
                "(Ljava/lang/Object;)Ljava/lang/Object;",
                false
        );
        method.visitTypeInsn(Opcodes.CHECKCAST, "io/github/ocelot/molangcompiler/api/object/MolangObject");
        method.visitVarInsn(Opcodes.ASTORE, index);

        return index;
    }

    /**
     * Loads whether the specified object has the specified variable onto the stack.
     *
     * @param method The method to insert code into
     * @param object The name of the object to query
     * @param name   The name of the variable to test for
     */
    public void loadObjectHas(MethodNode method, String object, String name) {
        if ("temp".equals(object)) {
            method.visitLdcInsn(this.variables.containsKey("temp." + name));
            return;
        }

        int objectIndex = this.getObjectIndex(method, object);
        method.visitVarInsn(Opcodes.ALOAD, objectIndex);
        method.visitLdcInsn(name);
        method.visitMethodInsn(
                Opcodes.INVOKEINTERFACE,
                "io/github/ocelot/molangcompiler/api/object/MolangObject",
                "has",
                "(Ljava/lang/String;)Z",
                true
        );
    }

    /**
     * Marks the specified object as requiring an update before returning.
     *
     * @param object The name of the object the variable is in
     * @param name   The name of the variable to mark dirty
     */
    public void markDirty(String object, String name) {
        // Don't try to save temporary variables
        if ("temp".equals(object)) {
            return;
        }
        this.modifiedVariables.add(object + "." + name);
    }

    /**
     * Writes all modified variables back into their objects.
     *
     * @param method The method to write values back into
     * @throws MolangSyntaxException If any error occurs with the format of the variables
     */
    public void writeModifiedVariables(MethodNode method) throws MolangSyntaxException {
        for (String name : this.modifiedVariables) {
            Integer index = this.variables.get(name);
            if (index == null) {
                throw new MolangSyntaxException("Unknown variable index: " + name);
            }

            String[] parts = name.split("\\.", 2);
            if (parts.length != 2) {
                throw new MolangSyntaxException("Expected 2 variable parts for " + name + ", got " + parts.length);
            }

            method.visitVarInsn(Opcodes.ALOAD, BytecodeCompiler.RUNTIME_INDEX);
            method.visitLdcInsn(parts[0]);
            method.visitLdcInsn(parts[1]);
            method.visitVarInsn(Opcodes.FLOAD, index);
            method.visitMethodInsn(
                    Opcodes.INVOKESTATIC,
                    "io/github/ocelot/molangcompiler/core/MolangUtil",
                    "setValue",
                    "(Lio/github/ocelot/molangcompiler/api/MolangEnvironment;Ljava/lang/String;Ljava/lang/String;F)V",
                    false
            );
        }
        this.modifiedVariables.clear();
    }
}
