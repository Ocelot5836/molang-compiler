import gg.moonflower.molangcompiler.api.exception.MolangSyntaxException;
import gg.moonflower.molangcompiler.core.compiler.MolangTokenizer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class MolangTokenizerTest {

    @Test
    public void testTokenize() throws MolangSyntaxException {
        String input = "temp.a = 14;temp.b=7";
        MolangTokenizer.Token[] tokens = MolangTokenizer.createTokens(input);
        System.out.println(Arrays.toString(tokens));
        Assertions.assertEquals(11, tokens.length);
    }

    @Test
    public void testBadTokens() throws MolangSyntaxException {
        String input = "temp.a=temp.b=temp.c=14;temp.b=7";
        MolangTokenizer.Token[] tokens = MolangTokenizer.createTokens(input);
        System.out.println(Arrays.toString(tokens));
        Assertions.assertEquals(19, tokens.length);
    }

    @Test
    public void testFunction() throws MolangSyntaxException {
        String input = "math.sin(42)";
        MolangTokenizer.Token[] tokens = MolangTokenizer.createTokens(input);
        System.out.println(Arrays.toString(tokens));
        Assertions.assertEquals(6, tokens.length);
    }

    @Test
    public void testFunctionParameters() throws MolangSyntaxException {
        String input = "math.clamp(7, 0, 4)";
        MolangTokenizer.Token[] tokens = MolangTokenizer.createTokens(input);
        System.out.println(Arrays.toString(tokens));
        Assertions.assertEquals(10, tokens.length);
    }
}
