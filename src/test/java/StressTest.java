import gg.moonflower.molangcompiler.api.MolangCompiler;
import gg.moonflower.molangcompiler.api.MolangEnvironment;
import gg.moonflower.molangcompiler.api.MolangExpression;
import gg.moonflower.molangcompiler.api.MolangRuntime;
import gg.moonflower.molangcompiler.api.exception.MolangException;
import org.junit.jupiter.api.Test;

import java.util.Map;

/**
 * @author Buddy
 */
public class StressTest {
    private static final int TEST_COUNT = 10_000_000;

    private static long elapsedParsingTime = 0;
    private static long elapsedExecutingBytecodeTime = 0;

    private static final MolangCompiler COMPILER = MolangCompiler.create();
    private static final MolangCompiler DEBUG_COMPILER = MolangCompiler.create(MolangCompiler.DEFAULT_FLAGS | MolangCompiler.WRITE_CLASSES_FLAG);
    private static final float[] PREVENT_OPTIMIZING = new float[TEST_COUNT];

    private static String longestTaken;
    private static long longestTakenTime = 0;

    @Test
    public void main() throws MolangException {
        test("query.a * query.b * query.c * query.d * query.e * query.f * query.g * query.h", 71192.2148438F,
                Map.of("a", 1.5F,
                        "b", 5.5F,
                        "c", 8.5F,
                        "d", 3.5F,
                        "e", 1.5F,
                        "f", 6.5F,
                        "g", 8.5F,
                        "h", 3.5F
                ));
        test(""
                + "{"
                + "	v.x = 5; "
                + "		{"
                + "		v.y = 2 * v.x;"
                + "			{"
                + "				v.z = 3 * v.y;"
                + "				return v.z;"
                + "			}"
                + "		}"
                + "}", 30.0F, Map.of());
        test("v.x = 20; loop(30, { v.x = v.x + 1 }); return v.x", 50.0F, Map.of());

        test("query.is_dashing * (query.anim_time + query.delta_time)", 8.25F, Map.of("is_dashing", 1.5F, "anim_time", 3.5F, "delta_time", 2.0F));
        test("query.climb_horizontal > 0.0 ? 1.0 - math.mod(0.25 * query.climb_horizontal, 1.0) : 0.25 * query.climb_horizontal", 0.625F, Map.of("climb_horizontal", 1.5F));

        test("5 + 3", 8.0F, Map.of());
        test("8 - 6", 2.0F, Map.of());
        test("6 / 2", 3.0F, Map.of());
        test("9 / 3", 3.0F, Map.of());
        test("9 / 0", Float.POSITIVE_INFINITY, Map.of());
        test("9 * 0", 0.0F, Map.of());
        test("4 * 3", 12.0F, Map.of());
        test("3 * 4", 12.0F, Map.of());
        test("(3*5) * 4", 60.0F, Map.of());
        test("(4*8) / 10", 3.2F, Map.of());

        test("0 && 0", 0.0F, Map.of());
        test("0 && 0 && 0", 0.0F, Map.of());
        test("0 && 0 && 1", 0.0F, Map.of());
        test("0 && 1", 0.0F, Map.of());
        test("0 && 1 && 0", 0.0F, Map.of());
        test("0 && 1 && 1", 0.0F, Map.of());
        test("1 && 0", 0.0F, Map.of());
        test("1 && 0 && 0", 0.0F, Map.of());
        test("1 && 0 && 1", 0.0F, Map.of());
        test("1 && 1", 1.0F, Map.of());
        test("1 && 1 && 0", 0.0F, Map.of());
        test("1 && 1 && 1", 1.0F, Map.of());
        test("0 || 0", 0.0F, Map.of());
        test("0 || 1", 1.0F, Map.of());
        test("1 || 0", 1.0F, Map.of());
        test("1 || 1", 1.0F, Map.of());
        test("0 || 0 || 0", 0.0F, Map.of());
        test("0 || 0 || 1", 1.0F, Map.of());
        test("0 || 1 || 0", 1.0F, Map.of());
        test("0 || 1 || 1", 1.0F, Map.of());
        test("1 || 0 || 0", 1.0F, Map.of());
        test("1 || 0 || 1", 1.0F, Map.of());
        test("1 || 1 || 0", 1.0F, Map.of());
        test("1 || 1 || 1", 1.0F, Map.of());

        test("10 > 10 ? 1 : 0", 0.0F, Map.of());
        test("10 < 10 ? 1 : 0", 0.0F, Map.of());
        test("10 >= 10 ? 1 : 0", 1.0F, Map.of());
        test("10 <= 10 ? 1 : 0", 1.0F, Map.of());
        test("5 > 5 ? 1 : 0", 0.0F, Map.of());
        test("5 < 5 ? 1 : 0", 0.0F, Map.of());
        test("5 >= 5 ? 1 : 0", 1.0F, Map.of());
        test("5 <= 5 ? 1 : 0", 1.0F, Map.of());
        test("5 > 2 ? 1 : 0", 1.0F, Map.of());
        test("2 > 5 ? 1 : 0", 0.0F, Map.of());
        test("5 < 2 ? 1 : 0", 0.0F, Map.of());
        test("2 < 5 ? 1 : 0", 1.0F, Map.of());

        test("(1 ? 16 : 3)", 16.0F, Map.of());
        test("(5 ? 10 : 6)", 10.0F, Map.of());
        test("(0 ? 10 : 9)", 9.0F, Map.of());

        test("query.a*5", 10, Map.of("a", 2.0F));

        for (float a : new float[]{5.0F}) {
            test("(((948 + (515)) - ((761 * (77)))) - (((query.a * (844)) / ((query.a / (query.a))))))", (((948.0F + (515.0F)) - ((761.0F * (77.0F)))) - (((a * (844.0F)) / ((a / (a)))))), Map.of("a", a));
            test("(((588 - (query.a)) + ((query.a - (978)))) * (((query.a + (263)) / ((592 / (189))))))", (((588.0F - (a)) + ((a - (978.0F)))) * (((a + (263.0F)) / ((592.0F / (189.0F)))))), Map.of("a", a));
            test("(((372 / (query.a)) - ((606 + (68)))) / (((query.a + (514)) / ((399 / (678))))))", (((372.0F / (a)) - ((606.0F + (68.0F)))) / (((a + (514.0F)) / ((399.0F / (678.0F)))))), Map.of("a", a));
            test("(((660 + (128)) * ((564 + (585)))) - (((614 + (query.a)) / ((query.a / (911))))))", (((660.0F + (128.0F)) * ((564.0F + (585.0F)))) - (((614.0F + (a)) / ((a / (911.0F)))))), Map.of("a", a));
            test("(((452 / (767)) + ((846 + (query.a)))) + (((552 / (52)) + ((662 - (369))))))", (((452.0F / (767.0F)) + ((846.0F + (a)))) + (((552.0F / (52.0F)) + ((662.0F - (369.0F)))))), Map.of("a", a));
            test("(((92 + (506)) / ((query.a / (query.a)))) - (((303 - (query.a)) / ((978 - (17))))))", (((92.0F + (506.0F)) / ((a / (a)))) - (((303.0F - (a)) / ((978.0F - (17.0F)))))), Map.of("a", a));
            test("(((876 * (query.a)) / ((377 + (query.a)))) * (((268 * (29)) + ((query.a / (58))))))", (((876.0F * (a)) / ((377.0F + (a)))) * (((268.0F * (29.0F)) + ((a / (58.0F)))))), Map.of("a", a));
            test("(((164 * (query.a)) - ((query.a / (850)))) * (((query.a / (query.a)) - ((662 + (307))))))", (((164.0F * (a)) - ((a / (850.0F)))) * (((a / (a)) - ((662.0F + (307.0F)))))), Map.of("a", a));
            test("(((956 * (query.a)) + ((query.a / (query.a)))) * (((query.a * (query.a)) + ((828 * (query.a))))))", (((956.0F * (a)) + ((a / (a)))) * (((a * (a)) + ((828.0F * (a)))))), Map.of("a", a));
            test("(((596 / (759)) / ((776 / (query.a)))) - (((query.a * (295)) + ((query.a + (844))))))", (((596.0F / (759.0F)) / ((776.0F / (a)))) - (((a * (295.0F)) + ((a + (844.0F)))))), Map.of("a", a));
            test("(((380 - (query.a)) + ((797 - (214)))) - (((591 / (480)) - ((query.a / (query.a))))))", (((380.0F - (a)) + ((797.0F - (214.0F)))) - (((591.0F / (480.0F)) - ((a / (a)))))), Map.of("a", a));
            test("(((668 - (query.a)) * ((query.a + (593)))) - (((query.a * (query.a)) - ((518 - (query.a))))))", (((668.0F - (a)) * ((a + (593.0F)))) - (((a * (a)) - ((518.0F - (a)))))), Map.of("a", a));
            test("(((812 - (query.a)) - ((query.a / (596)))) / (((query.a + (query.a)) + ((query.a / (31))))))", (((812.0F - (a)) - ((a / (596.0F)))) / (((a + (a)) + ((a / (31.0F)))))), Map.of("a", a));
            test("(((100 - (query.a)) + ((693 + (query.a)))) / (((query.a / (155)) - ((query.a / (860))))))", (((100.0F - (a)) + ((693.0F + (a)))) / (((a / (155.0F)) - ((a / (860.0F)))))), Map.of("a", a));
            test("(((884 / (498)) * ((query.a * (query.a)))) + (((query.a - (query.a)) + ((336 * (794))))))", (((884.0F / (498.0F)) * ((a * (a)))) + (((a - (a)) + ((336.0F * (794.0F)))))), Map.of("a", a));
            test("(((172 + (query.a)) / ((query.a + (query.a)))) * (((query.a * (query.a)) * ((228 - (292))))))", (((172.0F + (a)) / ((a + (a)))) * (((a * (a)) * ((228.0F - (292.0F)))))), Map.of("a", a));
        }

        System.out.println();
        System.out.println("Parsing time: " + formatTime(elapsedParsingTime));
        System.out.println("Execution (with bytecode): " + formatTime(elapsedExecutingBytecodeTime));
        System.out.println("Longest taken: " + longestTaken + " " + formatTime(longestTakenTime));

//        if (longestTaken != null) {
//            DEBUG_COMPILER.compile(longestTaken);
//        }
    }

    private static String formatTime(long t) {
        return Math.round(t) / 1000.0D + "ms";
    }

    public static void test(String expr, float exp, Map<String, Float> vars) throws MolangException {
        System.out.println("test(\"" + expr + "\", " + exp + "F);");

        MolangEnvironment environment = MolangRuntime.runtime()
                .setVariables(context -> vars.forEach((s, aFloat) -> context.addQuery(s, MolangExpression.of(aFloat))))
                .create();

        long time = System.nanoTime();
        MolangExpression expression = COMPILER.compile(expr);
        elapsedParsingTime += (System.nanoTime() - time) / 1000;

        time = System.nanoTime();
        for (int i = 0; i < TEST_COUNT; i++) {
            PREVENT_OPTIMIZING[i] = expression.get(environment);
        }
        if (PREVENT_OPTIMIZING[0] != exp) {
            throw new RuntimeException("Invalid bytecode value. Expected " + exp + ", but got " + PREVENT_OPTIMIZING[0] + " in expr \"" + expr + "\" transformed to \"" + expression + "\"");
        }
        long t = (System.nanoTime() - time) / 1000;
        if (t > longestTakenTime) {
            longestTakenTime = t;
            longestTaken = expr;
        }
        elapsedExecutingBytecodeTime += t;
    }
}