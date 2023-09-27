[![Jenkins](https://img.shields.io/jenkins/build?jobUrl=https://ci.blamejared.com/job/Foundry/job/molang-compiler/job/master/&style=?style=plastic)](https://ci.blamejared.com/job/Foundry/job/molang-compiler/job/master/)

# Molang Compiler

High Speed MoLang compiler and executor designed with per-frame execution in mind. Check the [MoLang Documentation](https://learn.microsoft.com/en-us/minecraft/creator/reference/content/molangreference/examples/molangconcepts/molangintroduction) for more information on how to write MoLang expressions.

# How to add to your workspace

There are two main ways to use this in your application. If you are writing a Minecraft Mod with Forge or Fabric,
install [Pollen](https://github.com/MoonflowerTeam/pollen) which already has the library shadowed. If you don't want
to add another library, you can just manually shadow this library into your mod.

```gradle
plugins {
    id 'com.github.johnrengelman.shadow' version "7.1.2"
}

configurations {
    shade
}

repositories {
    maven {
        name = "Jared's maven"
        url = "https://maven.blamejared.com/"
    }
}

dependencies {
    implementation "gg.moonflower:molang-compiler:version"
    shade "gg.moonflower:molang-compiler:version"
}

shadowJar {
    configurations = [project.configurations.shade]
    relocate 'gg.moonflower.molangcompiler', 'your.project.lib.molangcompiler'
}
```

This is only required in a modded workspace

```gradle
reobf {
    shadowJar {}
}

artifacts {
    archives jar
    archives shadowJar
}

build.dependsOn reobfShadowJar
```

# Usage

When using this library with a regular java program, you can use GlobalMolangCompiler to retrieve new instances of the
standard compiler. The compiler is designed this way to allow the garbage collector to delete old expressions if they
aren't needed anymore.

For example

```java
public class Main {

    public static void main(String[] args) {
        MolangCompiler compiler = GlobalMolangCompiler.get();
    }
}
```

When in an environment like Forge or Fabric a custom molang compiler instance must be created as a child of the mod
class loader. If using Pollen, this step is already handled.

```java

@Mod("modid")
public class ForgeMod {

    public ForgeMod() {
        MolangCompiler compiler = MolangCompiler.create(MolangCompiler.DEFAULT_FLAGS, ForgeMod.class.getClassLoader());
    }
}
```

# Extended Functionality

MoLang compiler supports some new features that are not in the Minecraft Bedrock MoLang spec.

- `math.sign(value)` returns 1 for positive values and -1 for negative values.
- `math.triangle_wave(value, period)` returns the position on a triangle wave from -1 to 1 with defined period.
- `++`, `+=`, `--`, `-=`, `/=`, and `*=` for mutable types. Eg: `v.test += 14`
- Expressions can execute java functions with float parameters. Eg: `custom_lib.javaFunction(param1, param2, param3)`

# Limitations

This implementation does not support the following:

- Structs, although there is support for variables with multiple parts. Eg: `v.pos.x = 4`
- Arrays
- Strings
- Arrow operator (`->`)
- For each operations (`for_each(<variable>, <array>, <expression>)`)
- Null-coalescing operators within loops (`loop(5, {v.test ?? 4})`)

# Examples

Compiling and using expressions:

```java
public class Example {

    private final MolangExpression speed;
    private final MolangExpression time;

    // MolangEnvironment#resolve(MolangExpression) allows the caller to handle errors created while resolving
    // The most common reason for an error is a variable being used that isn't defined in the environment
    public float getSpeed(MolangEnvironment environment) throws MolangRuntimeException {
        return environment.resolve(this.speed);
    }

    // Alternatively MolangEnvironment#safeResolve(MolangExpression) can be used to print the stack trace and return 0 on errors
    public float getTime(MolangEnvironment environment) {
        return environment.safeResolve(this.time);
    }

    public static @Nullable Example deserialize(String speedInput, String timeInput) {
        try {
            // Note: this cannot be used in a modded environment.
            // The compiler used should be a global instance
            // created like the ForgeMod example
            MolangCompiler compiler = GlobalMolangCompiler.get();

            // Expressions can be compiled from a valid MoLang string
            // Note that compilation is relatively expensive(~15ms sometimes) so it should be done once and cached
            MolangExpression speed = compiler.compile(speedInput);
            MolangExpression time = compiler.compile(timeInput);

            return new Example(speed, time);
        } catch (MolangSyntaxException e) {
            // The exception gives a message similar to Minecraft commands
            // indicating exactly what token was invalid
            e.printStackTrace();
            return null;
        }
    }
}
```

Using variables:

```java
public class Foo {

    public void run() {
        // A runtime is the base implementation of an environment.
        // The provided builder can add variables, queries, globals, and extra libraries
        MolangEnvironment environment = MolangRuntime.runtime()
                .setQuery("foo", 4)
                .setQuery("bar", 12)
                .create();

        Example example = Example.deserialize("(q.foo - q.bar) > 0", "q.foo * q.bar");
        // The environment will use the values specified in the builder as replacements when calculating the expression
        // In this example, the result will become 4 * 12 = 48
        float time = example.getTime(environment);

        // See the documentation for more details on adding java functions and variables
    }
}
```

Adding Custom MoLang Libraries

```java
public class BarLibrary extends MolangLibrary {

    @Override
    protected void populate(BiConsumer<String, MolangExpression> consumer) {
        // Add all expressions that should be registered under "libname"
        // For example, this becomes "libname.secret" in MoLang code
        consumer.accept("secret", MolangExpression.of(42));
    }

    // This name is used for printing and identification.
    // The actual namespace of the library is defined when adding it to a MolangEnvironment.
    @Override
    protected String getName() {
        return "libname";
    }

    public static void addLibraries(MolangEnvironment environment) {
        // Environments can be immutable and will throw an exception if they are tried to be modified
        if (!environment.canEdit()) {
            throw new UnsupportedOperationException("Environment is immutable");
        }

        MolangEnvironmentBuilder<? extends MolangEnvironment> builder = environment.edit();
        builder.setQuery("loadedBar", 1);

        // This will allow MoLang expressions to resolve "libname.secret" now
        environment.loadLibrary("libname", new BarLibrary());
    }
}
```
