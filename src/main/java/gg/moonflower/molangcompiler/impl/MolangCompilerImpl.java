package gg.moonflower.molangcompiler.impl;

import gg.moonflower.molangcompiler.api.MolangCompiler;
import gg.moonflower.molangcompiler.api.MolangExpression;
import gg.moonflower.molangcompiler.api.exception.MolangSyntaxException;
import gg.moonflower.molangcompiler.impl.ast.Node;
import gg.moonflower.molangcompiler.impl.compiler.BytecodeCompiler;
import gg.moonflower.molangcompiler.impl.compiler.MolangLexer;
import gg.moonflower.molangcompiler.impl.compiler.MolangParser;
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
