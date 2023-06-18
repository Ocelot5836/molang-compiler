package gg.moonflower.molangcompiler.api;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stores a global instance of the compiler for ease of access.
 *
 * @author Ocelot
 * @since 3.0.0
 */
public final class GlobalMolangCompiler {

    private static final Map<Integer, MolangCompiler> GLOBAL_COMPILERS = new ConcurrentHashMap<>();

    /**
     * Retrieves a compiler with the {@linkplain MolangCompiler#DEFAULT_FLAGS default flags}.
     *
     * @return The compiler instance
     */
    public static MolangCompiler get() {
        return get(MolangCompiler.DEFAULT_FLAGS);
    }

    /**
     * Retrieves a compiler with the specified flags. If unsure use {@link #get()}.
     *
     * @param flags The compiler flags to use
     * @return The compiler instance
     * @see MolangCompiler#OPTIMIZE_FLAG
     * @see MolangCompiler#WRITE_CLASSES_FLAG
     * @see MolangCompiler#DEFAULT_FLAGS
     */
    public static MolangCompiler get(int flags) {
        return GLOBAL_COMPILERS.computeIfAbsent(flags, MolangCompiler::create);
    }

    /**
     * Deletes the current instance of the compiler to allow compiled expression classes to be garbage collected.
     */
    public static void clear() {
        GLOBAL_COMPILERS.clear();
    }
}
