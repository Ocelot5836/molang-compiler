package gg.moonflower.molangcompiler.core;

import gg.moonflower.molangcompiler.api.MolangCompiler;
import gg.moonflower.molangcompiler.api.MolangExpression;
import gg.moonflower.molangcompiler.api.exception.MolangSyntaxException;
import gg.moonflower.molangcompiler.core.ast.Node;
import gg.moonflower.molangcompiler.core.compiler.BytecodeCompiler;
import gg.moonflower.molangcompiler.core.compiler.MolangLexer;
import gg.moonflower.molangcompiler.core.compiler.MolangTokenizer;
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

    public MolangExpression compile(String input) throws MolangSyntaxException {
        MolangTokenizer.Token[] tokens = MolangTokenizer.createTokens(input);
        Node node = MolangLexer.parseTokens(tokens);
        return this.compiler.build(node);
    }
}
