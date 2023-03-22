[![Release](https://jitpack.io/v/MoonflowerTeam/molang-compiler.svg)](https://jitpack.io/#MoonflowerTeam/molang-compiler)

# Molang Compiler

High Speed MoLang compiler and executor designed with per-frame execution in mind.

# How to add to your workspace

There are two main ways to use this in your application. If you are writing a Minecraft Mod with Forge or Fabric,
install [Pollen](https://github.com/MoonflowerTeam/pollen) which already has the library shadowed. If you do not want
to add another library to your mod, you can just manually shadow this library into your mod.

```gradle
plugins {
    id 'com.github.johnrengelman.shadow' version "4.0.4"
}

configurations {
    shade
}

repositories {
    maven {
        name = "JitPack"
        url = "https://jitpack.io"
    }
}

dependencies {
    implementation "com.github.MoonflowerTeam:molang-compiler:version"
    shade "com.github.MoonflowerTeam:molang-compiler:version"
}

shadowJar {
    configurations = [project.configurations.shade]
    relocate 'io.github.ocelot', 'your.project.lib.ocelot'
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
