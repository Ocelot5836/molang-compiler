import com.google.common.base.Stopwatch;
import io.github.ocelot.molangcompiler.api.MolangCompiler;
import io.github.ocelot.molangcompiler.api.MolangExpression;
import io.github.ocelot.molangcompiler.api.MolangRuntime;
import io.github.ocelot.molangcompiler.api.bridge.MolangVariable;
import io.github.ocelot.molangcompiler.api.exception.MolangException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MolangTest {

    @Test
    void testSpeed() throws MolangException {
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
                .create();
        Stopwatch runTime = Stopwatch.createStarted();
        float result = expression.resolve(runtime);
        runTime.stop();

        System.out.println("Took " + compileTime + " to compile, " + runTime + " to execute");
        System.out.println(expression + "\n==RESULT==\n" + result);
        Assertions.assertEquals(3, result);
    }

    @Test
    void testScopes() throws MolangException {
        MolangExpression expression = MolangCompiler.compile("""
                        temp.a = 4;
                        temp.b = 4;
                        {
                        temp.c = 1;
                        }
                        temp.d = 5;
                        return temp.d * temp.b;
                """);

        MolangRuntime runtime = MolangRuntime.runtime().create();
        float result = expression.resolve(runtime);
        System.out.println(expression + "\n==RESULT==\n" + result);
        Assertions.assertEquals(20, result);
    }

    @Test
    void testReturnScopes() throws MolangException {
        MolangExpression expression = MolangCompiler.compile("""
                        temp.a = 4;
                        temp.b = 2;
                        temp.c = {
                        return 4;
                        };
                        return {
                        return {return temp.a * temp.b;};
                        };
                """);

        MolangRuntime runtime = MolangRuntime.runtime().create();
        float result = expression.resolve(runtime);
        System.out.println(expression + "\n==RESULT==\n" + result);
        Assertions.assertEquals(8, result);
    }

    @Test
    void testRandom() throws MolangException {
        MolangExpression expression = MolangCompiler.compile("math.die_roll(1, 0, 1)");

        MolangRuntime runtime = MolangRuntime.runtime().create();
        float result = expression.resolve(runtime);
        System.out.println(expression + "\n==RESULT==\n" + result);
    }

    MolangVariable testVariable = MolangVariable.create(7);

    @Test
    void testGetVariable() throws MolangException {
        MolangExpression expression = MolangCompiler.compile("""
                t.a = 14;
                return v.test + t.a;
                """);

        MolangRuntime runtime = MolangRuntime.runtime()
                .setVariable("test", this.testVariable).create();
        float result = expression.resolve(runtime);
        System.out.println(expression + "\n==RESULT==\n" + result);
        Assertions.assertEquals(21, result);
    }

    @Test
    void testSetVariable() throws MolangException {
        MolangExpression expression = MolangCompiler.compile("""
                v.test = 2;
                """);

        MolangRuntime runtime = MolangRuntime.runtime()
                .setVariable("test", this.testVariable).create();
        float result = expression.resolve(runtime);
        System.out.println(expression + "\n==RESULT==\n" + result);
        Assertions.assertEquals(2, result);
        Assertions.assertEquals(2, this.testVariable.getValue());
    }

    @Test
    void testMultiple() throws MolangException {
        MolangExpression expression = MolangCompiler.compile("v.b = 2;v.a = 3;v.ab = v.b;v.c = 1;");

        MolangRuntime runtime = MolangRuntime.runtime().create();
        float result = expression.resolve(runtime);
        System.out.println(expression + "\n==RESULT==\n" + result);
    }

    @Test
    void testCondition() throws MolangException {
        MolangExpression expression = MolangCompiler.compile("1 > 2 ? 10 : 20", 0);

        MolangRuntime runtime = MolangRuntime.runtime().create();
        float result = expression.resolve(runtime);
        System.out.println(expression + "\n==RESULT==\n" + result);
        Assertions.assertEquals(20, result);
    }

    @Test
    void testComplexCondition() throws MolangException {
        MolangExpression expression = MolangCompiler.compile("math.clamp(0.5 + variable.particle_random_4/7 + (variable.particle_random_3>0.2 ? 0.4 : 0), 0, 1)");

        MolangRuntime runtime = MolangRuntime.runtime().create();
        float result = expression.resolve(runtime);
        System.out.println(expression + "\n==RESULT==\n" + result);
        Assertions.assertEquals(0.5, result);
    }

    @Test
    void testNegativeCondition() throws MolangException {
        MolangExpression expression = MolangCompiler.compile("+variable.particle_random_3>0.2 ? -10 : -4");

        MolangRuntime runtime = MolangRuntime.runtime().create();
        float result = expression.resolve(runtime);
        System.out.println(expression + "\n==RESULT==\n" + result);
        Assertions.assertEquals(-4, result);
    }

    @Test
    void testWeird() throws MolangException {
        MolangExpression expression = MolangCompiler.compile("((((-7))*((((((((variable.particle_random_3>(0.2) * (((4))) ? (-10) : -4))))))))))");

        MolangRuntime runtime = MolangRuntime.runtime().create();
        float result = expression.resolve(runtime);
        System.out.println(expression + "\n==RESULT==\n" + result);
        Assertions.assertEquals(28, result);
    }
}
