package gg.moonflower.molangcompiler.core.compiler;

import gg.moonflower.molangcompiler.api.MolangCompiler;
import gg.moonflower.molangcompiler.api.MolangExpression;
import gg.moonflower.molangcompiler.api.exception.MolangSyntaxException;
import gg.moonflower.molangcompiler.core.ast.Node;
import org.jetbrains.annotations.ApiStatus;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * @author Buddy, Ocelot
 */
@ApiStatus.Internal
public class BytecodeCompiler extends ClassLoader {

    public static final int FLAG_OPTIMIZE = 1;

    public static final int THIS_INDEX = 0;
    public static final int RUNTIME_INDEX = 1;
    public static final int VARIABLE_START = 2;

    private final MolangBytecodeEnvironment environment;
    private final boolean writeClasses;

    public BytecodeCompiler(int flags) {
        this.environment = new MolangBytecodeEnvironment(flags);
        this.writeClasses = (flags & MolangCompiler.WRITE_CLASSES_FLAG) > 0;
    }

    public MolangExpression build(Node node) throws MolangSyntaxException {
        this.environment.reset();
        try {
            if (this.environment.optimize() && node.isConstant()) {
                return MolangExpression.of(node.evaluate(this.environment));
            }

            ClassNode classNode = new ClassNode(Opcodes.ASM5);
            classNode.version = Opcodes.V1_8;
            classNode.superName = "java/lang/Object";
            classNode.name = "Expression_" + System.nanoTime();
            classNode.access = Opcodes.ACC_PUBLIC;
            classNode.interfaces.add("gg/moonflower/molangcompiler/api/MolangExpression");

            MethodNode init = new MethodNode();
            init.access = Opcodes.ACC_PUBLIC;
            init.name = "<init>";
            init.desc = "()V";
            init.visitVarInsn(Opcodes.ALOAD, 0);
            init.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
            init.visitInsn(Opcodes.RETURN);
            classNode.methods.add(init);

            MethodNode method = new MethodNode();
            method.access = Opcodes.ACC_PUBLIC;
            method.name = "get";
            method.desc = "(Lgg/moonflower/molangcompiler/api/MolangEnvironment;)F";
            method.exceptions = List.of("gg/moonflower/molangcompiler/api/exception/MolangRuntimeException");
            node.writeBytecode(method, this.environment, null, null);
            classNode.methods.add(method);

            MethodNode toString = new MethodNode();
            toString.access = Opcodes.ACC_PUBLIC;
            toString.name = "toString";
            toString.desc = "()Ljava/lang/String;";
            toString.visitLdcInsn(node.toString());
            toString.visitInsn(Opcodes.ARETURN);
            toString.visitInsn(Opcodes.RETURN);
            classNode.methods.add(toString);

            ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
            classNode.accept(cw);
            byte[] data = cw.toByteArray();

            if (this.writeClasses) {
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
}