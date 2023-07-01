package gg.moonflower.molangcompiler.core;

import gg.moonflower.molangcompiler.api.MolangCompiler;
import gg.moonflower.molangcompiler.api.MolangExpression;
import gg.moonflower.molangcompiler.api.exception.MolangSyntaxException;
import gg.moonflower.molangcompiler.core.ast.Node;
import gg.moonflower.molangcompiler.core.compiler.BytecodeCompiler;
import gg.moonflower.molangcompiler.core.compiler.MolangParser;
import gg.moonflower.molangcompiler.core.compiler.MolangLexer;
import org.jetbrains.annotations.ApiStatus;

/**
 * @author Ocelot
 */
@ApiStatus.Internal
public class MolangCompilerImpl implements MolangCompiler {

    private final BytecodeCompiler compiler;

    public MolangCompilerImpl(int flags) {
        this.compiler = new BytecodeCompiler(flags);
    }

    public MolangCompilerImpl(int flags, ClassLoader classLoader) {
        this.compiler = new BytecodeCompiler(flags, classLoader);
    }

    public MolangExpression compile(String input) throws MolangSyntaxException {
        MolangLexer.Token[] tokens = MolangLexer.createTokens(input);
        Node node = MolangParser.parseTokens(tokens);
        return this.compiler.build(node);
    }
}
