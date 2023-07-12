import gg.moonflower.molangcompiler.api.MolangExpression;
import gg.moonflower.molangcompiler.api.MolangRuntime;
import gg.moonflower.molangcompiler.api.exception.MolangRuntimeException;
import gg.moonflower.molangcompiler.api.object.MolangObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MolangBuilderTest {

    @Test
    public void testEdit() throws MolangRuntimeException {
        MolangRuntime runtime = MolangRuntime.runtime().create();
        MolangRuntime edit = runtime.edit().setGlobal("test", 44).create();

        Assertions.assertEquals(runtime, edit);

        MolangObject global = runtime.get("global");
        Assertions.assertEquals(1, global.getKeys().size());
        Assertions.assertEquals(MolangExpression.of(44), global.get("test"));
    }

    @Test
    public void testCopy() throws MolangRuntimeException {
        MolangRuntime runtime = MolangRuntime.runtime().create();
        MolangRuntime edit = runtime.edit().setGlobal("test", 44).create();

        Assertions.assertEquals(1, edit.get("global").getKeys().size());

        Assertions.assertEquals(runtime, edit);
        MolangRuntime copy1 = MolangRuntime.runtime().copy(runtime).setGlobal("test", 4).create();
        MolangRuntime copy = MolangRuntime.runtime().copy(runtime).setGlobal("test2", 4).create();
        Assertions.assertEquals(1, runtime.get("global").getKeys().size());
        Assertions.assertEquals(1, copy1.get("global").getKeys().size());
        Assertions.assertEquals(2, copy.get("global").getKeys().size());
    }
}
