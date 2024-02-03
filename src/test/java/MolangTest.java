import com.google.common.base.Stopwatch;
import gg.moonflower.molangcompiler.api.MolangCompiler;
import gg.moonflower.molangcompiler.api.MolangExpression;
import gg.moonflower.molangcompiler.api.MolangRuntime;
import gg.moonflower.molangcompiler.api.bridge.MolangVariable;
import gg.moonflower.molangcompiler.api.exception.MolangException;
import gg.moonflower.molangcompiler.api.exception.MolangSyntaxException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class MolangTest {

    @Test
    void testSpeed() throws MolangException {
        MolangCompiler compiler = MolangCompiler.create();
        compiler.compile("0"); // load

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
                compiler.compile("""
                        math.trunc(math.pi);
                        v.test = q.anim_time;
                        v.test/=30;
                        v.test--;
                        v.test--;
                        v.test--;
                        v.test+=3;
                        return v.test;
                        """);
        compileTime.stop();

        MolangRuntime runtime = MolangRuntime.runtime()
                .setQuery("anim_time", 90)
                .setQuery("life_time", 0)
                .create();

        System.out.println(expression);

        int iterations = 10000;
        long[] times = new long[iterations];
        for (int i = 0; i < iterations; i++) {
            Stopwatch runTime = Stopwatch.createStarted();
            float result = runtime.resolve(expression);
            runTime.stop();

            Assertions.assertEquals(3, result);
            times[i] = runTime.elapsed(TimeUnit.NANOSECONDS);
        }

        System.out.println("Took " + compileTime + " to compile, " + Arrays.stream(times).average().orElse(0) + "ns to execute " + iterations + " times");
    }

    @Test
    void testSimplify() throws MolangException {
        MolangCompiler compiler = MolangCompiler.create();
        MolangExpression expression = compiler.compile("math.pi*2+(3/2+53)*((7)/5)");

        MolangRuntime runtime = MolangRuntime.runtime().create();
        float result = runtime.resolve(expression);
        System.out.println(expression + "\n==RESULT==\n" + result);
        Assertions.assertEquals(82.58318328857422, result);
    }

    @Test
    void testScopes() throws MolangException {
        MolangCompiler compiler = MolangCompiler.create();
        MolangExpression expression = compiler.compile("""
                        temp.a = 4;
                        temp.b = 4;
                        {
                        temp.c = 1;
                        }
                        temp.d = 5;
                        return temp.d * temp.b;
                """);

        MolangRuntime runtime = MolangRuntime.runtime().create();
        float result = runtime.resolve(expression);
        System.out.println(expression + "\n==RESULT==\n" + result);
        Assertions.assertEquals(20, result);
    }

    @Test
    void testReturnScopes() throws MolangException {
        MolangCompiler compiler = MolangCompiler.create();
        MolangExpression expression = compiler.compile("""
                        temp.a = 4;
                        temp.b = 2;
                        temp.c = {
                        4;
                        };
                        return {
                        {temp.a * temp.b;};
                        };
                """);

        MolangRuntime runtime = MolangRuntime.runtime().create();
        float result = runtime.resolve(expression);
        System.out.println(expression + "\n==RESULT==\n" + result);
        Assertions.assertEquals(8, result);
    }

    @Test
    void testRandom() throws MolangException {
        MolangCompiler compiler = MolangCompiler.create();
        MolangExpression expression = compiler.compile("math.die_roll(1, 0, 10)");

        MolangRuntime runtime = MolangRuntime.runtime().create();
        float result = runtime.resolve(expression);
        System.out.println(expression + "\n==RESULT==\n" + result);
    }

    MolangVariable testVariable = MolangVariable.create(7);

    @Test
    void testGetVariable() throws MolangException {
        MolangCompiler compiler = MolangCompiler.create();
        MolangExpression expression = compiler.compile("""
                t.a = 14;
                return v.test + t.a;
                """);

        MolangRuntime runtime = MolangRuntime.runtime()
                .setVariable("test", this.testVariable).create();
        float result = runtime.resolve(expression);
        System.out.println(expression + "\n==RESULT==\n" + result);
        Assertions.assertEquals(21, result);
    }

    @Test
    void testSetVariable() throws MolangException {
        MolangCompiler compiler = MolangCompiler.create();
        MolangExpression expression = compiler.compile("""
                v.test = 2;
                """);

        MolangRuntime runtime = MolangRuntime.runtime()
                .setVariable("test", this.testVariable).create();
        float result = runtime.resolve(expression);
        System.out.println(expression + "\n==RESULT==\n" + result);
        Assertions.assertEquals(2, result);
        Assertions.assertEquals(2, this.testVariable.getValue());
    }

    @Test
    void testMultiple() throws MolangException {
        MolangCompiler compiler = MolangCompiler.create();
        MolangExpression expression = compiler.compile("v.b = 2;v.a = 3;v.ab = v.b;v.c = 1;");

        MolangRuntime runtime = MolangRuntime.runtime().create();
        float result = runtime.resolve(expression);
        System.out.println(expression + "\n==RESULT==\n" + result);
    }

    @Test
    void testCondition() throws MolangException {
        MolangCompiler compiler = MolangCompiler.create(0);
        MolangExpression expression = compiler.compile("1 > 2 ? 10 : 20");

        MolangRuntime runtime = MolangRuntime.runtime().create();
        float result = runtime.resolve(expression);
        System.out.println(expression + "\n==RESULT==\n" + result);
        Assertions.assertEquals(20, result);
    }

    @Test
    void testComplexCondition() throws MolangException {
        MolangCompiler compiler = MolangCompiler.create();
        MolangExpression expression = compiler.compile("math.clamp(0.5 + variable.particle_random_4/7 + (variable.particle_random_3>0.2 ? 0.4 : 0), 0, 1)");

        MolangRuntime runtime = MolangRuntime.runtime()
                .setVariable("particle_random_1", MolangExpression.ZERO)
                .setVariable("particle_random_2", MolangExpression.ZERO)
                .setVariable("particle_random_3", MolangExpression.ZERO)
                .setVariable("particle_random_4", MolangExpression.ZERO)
                .create();
        float result = runtime.resolve(expression);
        System.out.println(expression + "\n==RESULT==\n" + result);
        Assertions.assertEquals(0.5, result);
    }

    @Test
    void testNegativeCondition() throws MolangException {
        MolangCompiler compiler = MolangCompiler.create();
        MolangExpression expression = compiler.compile("+variable.particle_random_3??0>0.2 ? -10 : -4");

        MolangRuntime runtime = MolangRuntime.runtime().create();
        float result = runtime.resolve(expression);
        System.out.println(expression + "\n==RESULT==\n" + result);
        Assertions.assertEquals(-4, result);
    }

    @Test
    void testWeird() throws MolangException {
        MolangCompiler compiler = MolangCompiler.create();
        MolangExpression expression = compiler.compile("((((-7))*((((((((variable.particle_random_3>(0.2) * (((4))) ? (-10) : -4))))))))))");

        MolangRuntime runtime = MolangRuntime.runtime()
                .setVariable("particle_random_3", MolangExpression.ZERO)
                .create();
        float result = runtime.resolve(expression);
        System.out.println(expression + "\n==RESULT==\n" + result);
        Assertions.assertEquals(28, result);
    }

    @Test
    void testCopy() {
        MolangRuntime.Builder builder1 = (MolangRuntime.Builder) MolangRuntime.runtime().setVariable("test", MolangVariable.create(10));
        MolangRuntime.Builder builder2 = (MolangRuntime.Builder) MolangRuntime.runtime(builder1).setVariable("test2", MolangVariable.create(100));

        System.out.println("builder1");
        System.out.println(builder1.create().dump());
        System.out.println("builder2");
        System.out.println(builder2.create().dump());
    }

    @Test
    void testContainer() throws MolangException {
        MolangCompiler compiler = MolangCompiler.create();
        MolangExpression expression = compiler.compile("v.screen_aspect_ratio > v.aspect_ratio ? q.screen.width : q.screen.height * v.aspect_ratio");

        MolangRuntime runtime = MolangRuntime.runtime()
                .setVariable("screen_aspect_ratio", MolangExpression.of(7))
                .setVariable("aspect_ratio", MolangExpression.of(2))
                .setQuery("screen.width", MolangExpression.of(12))
                .setQuery("screen.height", MolangExpression.of(12))
                .create();
        float result = runtime.resolve(expression);
        System.out.println(expression + "\n==RESULT==\n" + result);
        Assertions.assertEquals(12, result);
    }

    @Test
    void testImmutable() throws MolangException {
        MolangCompiler compiler = MolangCompiler.create();
        MolangVariable test = MolangVariable.create(1);
        MolangVariable testImmutable = test.immutable();

        MolangExpression expression = compiler.compile("v.test=2");

        MolangRuntime runtime = MolangRuntime.runtime().setVariable("test", testImmutable).create();
        float result = runtime.resolve(expression);
        System.out.println(expression + "\n==RESULT==\n" + result);
        Assertions.assertEquals(1, test.getValue());
    }

    @Test
    void testCompare() throws MolangException {
        MolangCompiler compiler = MolangCompiler.create();
        MolangExpression expression = compiler.compile("5<5");

        MolangRuntime runtime = MolangRuntime.runtime().create();
        float result = runtime.resolve(expression);
        System.out.println(expression + "\n==RESULT==\n" + result);
        Assertions.assertEquals(0.0F, result);
    }

    @Test
    void testSign() throws MolangException {
        MolangCompiler compiler = MolangCompiler.create();
        MolangExpression expression = compiler.compile("math.sign(-4)");

        MolangRuntime runtime = MolangRuntime.runtime().create();
        float result = runtime.resolve(expression);
        System.out.println(expression + "\n==RESULT==\n" + result);
        Assertions.assertEquals(-1.0F, result);
    }

    @Test
    void testTriangleWave() throws MolangException {
        MolangCompiler compiler = MolangCompiler.create();
        MolangExpression expression = compiler.compile("math.triangle_wave(23544/2, 23544)");

        MolangRuntime runtime = MolangRuntime.runtime().create();
        float result = runtime.resolve(expression);
        System.out.println(expression + "\n==RESULT==\n" + result);
        Assertions.assertEquals(-1.0F, result);
    }

    @Test
    void testEqualsHashCode() throws MolangSyntaxException {
        MolangCompiler compiler = MolangCompiler.create();
        MolangExpression expression1 = compiler.compile("q.test");
        MolangExpression expression2 = compiler.compile("q.test");

        Assertions.assertEquals(expression1, expression2);
        Assertions.assertEquals(expression1.hashCode(), expression2.hashCode());
        Assertions.assertNotEquals("q.test", expression1);
        Assertions.assertNotEquals("q.test", expression2);
    }

    @Test
    void testAState() throws MolangSyntaxException {
        MolangCompiler compiler = MolangCompiler.create();
        compiler.compile("query.is_gliding == 1.0 ? 1.0 : 0.0");
        compiler.compile("query.is_gliding");
    }

    @Test
    void testBState() throws MolangSyntaxException {
        MolangCompiler compiler = MolangCompiler.create();
        compiler.compile("test.a ? 4 : 0");
    }

    @Test
    void testJavaCondition() throws MolangException {
        MolangCompiler compiler = MolangCompiler.create();
        MolangExpression eq = compiler.compile("query.climb_vertical == 0.0");
        MolangExpression ne = compiler.compile("query.climb_vertical != 0.0");
        MolangExpression gt = compiler.compile("query.climb_vertical > 0.0");
        MolangExpression gteq = compiler.compile("query.climb_vertical >= 0.0");
        MolangExpression lt = compiler.compile("query.climb_vertical < 0.0");
        MolangExpression lteq = compiler.compile("query.climb_vertical <= 0.0");

        MolangRuntime runtime = MolangRuntime.runtime().create();
        for (int i = -4; i < 6; i++) {
            runtime.edit().setQuery("climb_vertical", i);
            Assertions.assertEquals(i == 0 ? 1.0F : 0.0F, runtime.resolve(eq));
            Assertions.assertEquals(i != 0 ? 1.0F : 0.0F, runtime.resolve(ne));
            Assertions.assertEquals(i > 0 ? 1.0F : 0.0F, runtime.resolve(gt));
            Assertions.assertEquals(i >= 0 ? 1.0F : 0.0F, runtime.resolve(gteq));
            Assertions.assertEquals(i < 0 ? 1.0F : 0.0F, runtime.resolve(lt));
            Assertions.assertEquals(i <= 0 ? 1.0F : 0.0F, runtime.resolve(lteq));
        }
    }

    @Test
    void testTrueFalse() throws MolangException {
        MolangCompiler compiler = MolangCompiler.create();
        MolangExpression trueExpression = compiler.compile("true");
        MolangExpression falseExpression = compiler.compile("false");

        MolangRuntime runtime = MolangRuntime.runtime().create();
        float trueResult = runtime.resolve(trueExpression);
        float falseResult = runtime.resolve(falseExpression);
        Assertions.assertEquals(1.0F, trueResult);
        Assertions.assertEquals(0.0F, falseResult);
    }

    @Test
    void testLoop() throws MolangException {
        MolangCompiler compiler = MolangCompiler.create();
        MolangExpression loop = compiler.compile("""
                temp.i = 0;
                loop(10, {
                    temp.i++;
                    if(temp.i > 5) {
                        temp.i+=3;
                        break;
                    } else {
                        temp.i+=2;
                        continue;
                    }
                });
                temp.i;
                """);

        MolangRuntime runtime = MolangRuntime.runtime().create();
        float result = runtime.resolve(loop);
        Assertions.assertEquals(10, result);
    }

    @Test
    void testIf() throws MolangException {
        MolangCompiler compiler = MolangCompiler.create();
        MolangExpression loop = compiler.compile("""
                if(true) {
                    return 4;
                }
                return 1;
                """);

        MolangRuntime runtime = MolangRuntime.runtime().create();
        float result = runtime.resolve(loop);
        Assertions.assertEquals(4, result);
    }
}
