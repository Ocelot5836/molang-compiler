package io.github.ocelot.molangcompiler.core.compiler;

import io.github.ocelot.molangcompiler.api.MolangExpression;
import io.github.ocelot.molangcompiler.api.exception.MolangSyntaxException;
import io.github.ocelot.molangcompiler.core.ast.Node;
import org.jetbrains.annotations.ApiStatus;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Buddy, Ocelot
 */
@ApiStatus.Internal
public class BytecodeCompiler extends ClassLoader {

    public static final int FLAG_OPTIMIZE = 1;

    public static final int THIS_INDEX = 0;
    public static final int RUNTIME_INDEX = 1;
    public static final int VARIABLE_START = 2;

    private static final boolean DEBUG_WRITE_CLASSES = false;

    private final MolangBytecodeEnvironment environment;
    private final AtomicInteger compileId;

    public BytecodeCompiler(int flags) {
        this.environment = new MolangBytecodeEnvironment(flags);
        this.compileId = new AtomicInteger();
    }

    public MolangExpression build(Node node) throws MolangSyntaxException {
        this.environment.reset();
        try {
            if (this.environment.optimize() && node.isConstant()) {
                float value = node.evaluate(this.environment);
                return unused -> value;
            }

            ClassNode classNode = new ClassNode(Opcodes.ASM5);
            classNode.version = Opcodes.V1_8;
            classNode.superName = "java/lang/Object";
            classNode.name = "Expression_" + this.compileId.getAndIncrement();
            classNode.access = Opcodes.ACC_PUBLIC;
            classNode.interfaces.add("io/github/ocelot/molangcompiler/api/MolangExpression");

            MethodNode init = new MethodNode();
            init.access = Opcodes.ACC_PUBLIC;
            init.name = "<init>";
            init.desc = "()V";
            init.exceptions = new ArrayList<>();
            init.visitVarInsn(Opcodes.ALOAD, 0);
            init.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
            init.visitInsn(Opcodes.RETURN);
            classNode.methods.add(init);

            MethodNode method = new MethodNode();
            method.access = Opcodes.ACC_PUBLIC;
            method.name = "get";
            method.desc = "(Lio/github/ocelot/molangcompiler/api/MolangEnvironment;)F";
            method.exceptions = List.of("io/github/ocelot/molangcompiler/api/exception/MolangRuntimeException");

            // Populate method
            node.writeBytecode(method, this.environment, null, null);

            classNode.methods.add(method);

            ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
            classNode.accept(cw);
            byte[] data = cw.toByteArray();

            if (DEBUG_WRITE_CLASSES) {
                Path path = Paths.get(classNode.name + ".class");
                if (!Files.exists(path)) {
                    Files.createFile(path);
                }
                Files.write(path, data);
            }

            return (MolangExpression) this.defineClass(classNode.name, data, 0, data.length).getConstructor().newInstance();
        } catch (Throwable t) {
            throw new MolangSyntaxException("Failed to convert expression '" + node + "' to bytecode", t);
        }
    }

    public static void writeFloatConst(MethodNode method, float value) {
        if (value == 0.0F) {
            method.visitInsn(Opcodes.FCONST_0);
        } else if (value == 1.0F) {
            method.visitInsn(Opcodes.FCONST_1);
        } else if (value == 2.0F) {
            method.visitInsn(Opcodes.FCONST_2);
        } else {
            method.visitLdcInsn(value);
        }
    }

    public static void writeIntConst(MethodNode method, int value) {
        switch (value) {
            case 0 -> method.visitInsn(Opcodes.ICONST_0);
            case 1 -> method.visitInsn(Opcodes.ICONST_1);
            case 2 -> method.visitInsn(Opcodes.ICONST_2);
            case 3 -> method.visitInsn(Opcodes.ICONST_3);
            case 4 -> method.visitInsn(Opcodes.ICONST_4);
            case 5 -> method.visitInsn(Opcodes.ICONST_5);
            default -> {
                if (value < Byte.MAX_VALUE) {
                    method.visitIntInsn(Opcodes.BIPUSH, (byte) value);
                } else if (value < Short.MAX_VALUE) {
                    method.visitIntInsn(Opcodes.SIPUSH, (short) value);
                } else {
                    method.visitLdcInsn(value);
                }
            }
        }
    }

//    public static void main(String[] args) {
//        try {
//            BytecodeCompiler compiler = new BytecodeCompiler(FLAG_OPTIMIZE);
//            compiler.build(new ReturnNode(
//                    new CompoundNode(
//                            new LoopNode(
//                                    new BinaryOperationNode(BinaryOperation.NULL_COALESCING, new VariableGetNode("variable", "test"), new ConstNode(4)),
//                                    new CompoundNode()
//                            ),
//                            new ConstNode(1))
//            ));
//            compiler.build(new ReturnNode(new FunctionNode("query", "test", new BinaryOperationNode(BinaryOperation.NULL_COALESCING, new VariableGetNode("variable", "test"), new ConstNode(4)), new ConstNode(4), new ConstNode(2))));
//            compiler.build(new ReturnNode(new CompoundNode(
//                    new VariableSetNode("variable", "test", new ConstNode(4)),
//                    new BinaryOperationNode(BinaryOperation.MULTIPLY, new VariableGetNode("variable", "test"), new ConstNode(4))
//            )));
//            compiler.build(new ReturnNode(new ThisNode()));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}