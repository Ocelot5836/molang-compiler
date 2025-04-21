package gg.moonflower.molangcompiler.api;

import gg.moonflower.molangcompiler.api.exception.MolangSyntaxException;
import gg.moonflower.molangcompiler.impl.MolangCompilerImpl;

/**
 * <p>Compiles a {@link MolangExpression} from a string input.</p>
 * <p>A compiler instance must be created to allow garbage collection of generated classes when no longer in use.</p>
 *
 * @author Ocelot
 * @see GlobalMolangCompiler
 * @since 3.0.0
 */
public interface MolangCompiler {

    /**
     * Whether to reduce math to constant values if possible. E.g. <code>4 * 4 + 2</code> would become <code>18</code>. This should almost always be on.
     */
    int OPTIMIZE_FLAG = 0b01;
    /**
     * Whether to write the java bytecode to a class file. This is only for debugging.
     */
    int WRITE_CLASSES_FLAG = 0b10;

    /**
     * All default compilation flags. This may change in future versions as more options are added.
     */
    int DEFAULT_FLAGS = OPTIMIZE_FLAG;

    /**
     * Compiles a {@link MolangExpression} from the specified string input.
     *
     * @param input The data to compile
     * @return The compiled expression
     * @throws MolangSyntaxException If any error occurs
     */
    MolangExpression compile(String input) throws MolangSyntaxException;

    /**
     * Creates a compiler with the {@linkplain MolangCompiler#DEFAULT_FLAGS default flags}.
     *
     * @return The compiler instance
     */
    static MolangCompiler create() {
        return new MolangCompilerImpl(DEFAULT_FLAGS);
    }

    /**
     * Creates a compiler with the specified flags. If unsure use {@link #create()}.
     *
     * @param flags The compiler flags to use
     * @return The compiler instance
     * @see MolangCompiler#OPTIMIZE_FLAG
     * @see MolangCompiler#WRITE_CLASSES_FLAG
     * @see MolangCompiler#DEFAULT_FLAGS
     */
    static MolangCompiler create(int flags) {
        return new MolangCompilerImpl(flags);
    }

    /**
     * Creates a compiler with the specified flags. If unsure use {@link #create()}.
     *
     * @param flags  The compiler flags to use
     * @param parent The classloader to use as the parent.
     *               This should only be set when the current class is not using the system class loader
     * @return The compiler instance
     * @see MolangCompiler#OPTIMIZE_FLAG
     * @see MolangCompiler#WRITE_CLASSES_FLAG
     * @see MolangCompiler#DEFAULT_FLAGS
     */
    static MolangCompiler create(int flags, ClassLoader parent) {
        return new MolangCompilerImpl(flags, parent);
    }
}
