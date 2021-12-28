import com.google.common.base.Stopwatch;
import io.github.ocelot.molangcompiler.api.MolangCompiler;
import io.github.ocelot.molangcompiler.api.MolangExpression;
import io.github.ocelot.molangcompiler.api.MolangRuntime;
import io.github.ocelot.molangcompiler.api.exception.MolangException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class MolangTest
{
    @Test
    void testSpeed() throws MolangException
    {
        Stopwatch compileTime = Stopwatch.createStarted();
        MolangExpression expression =
//                MolangCompiler.compile("2");
//                MolangCompiler.compile("temp.my_temp_var = math.sin(90) / 2");
//        MolangCompiler.compile("return math.sin(global.anim_time * 1.23)");
//        MolangCompiler.compile("math.sin(global.anim_time * 1.23)");
//        MolangCompiler.compile("(math.cos(query.life_time * 20.0 * 10.89) * 28.65) + (math.sin(variable.attack_time * 180.0) * 68.76 - (math.sin((1.0 - (1.0 - variable.attack_time) * (1.0 - variable.attack_time)) * 180.0)) * 22.92)");
//        MolangCompiler.compile("temp.my_temp_var = Math.sin(query.anim_time * 1.23);\n" +
//                "temp.my_other_temp_var = Math.cos(query.life_time + 2.0);\n" +
//                "return temp.my_temp_var * temp.my_temp_var + temp.my_other_temp_var;");
                MolangCompiler.compile("math.trunc(math.pi)");
        compileTime.stop();

        MolangRuntime runtime = MolangRuntime.runtime()
                .setQuery("anim_time", 90)
                .setQuery("life_time", 0)
                .create(0);


        Stopwatch runTime = Stopwatch.createStarted();
        float result = expression.resolve(runtime);
        runTime.stop();

        Assertions.assertEquals(result, 3);

        System.out.println("\n" + runtime.dump());
        System.out.println("Took " + compileTime + " to compile, " + runTime + " to execute");
    }

    @Test
    void testScopes() throws MolangException
    {
        MolangExpression expression = MolangCompiler.compile("""
                        temp.a = 4;
                        {
                        temp.b = 16;
                        }
                        temp.b = temp.c * 4;
                        return temp.b;
                """);

        MolangRuntime runtime = MolangRuntime.runtime().create(0.0F);

        System.out.println(expression + "\n==RESULT==\n" + expression.resolve(runtime));
    }
}
