# Build Logic
This repo stores the common build logic that is shared by my personal projects.

## Usage

### Importing the build logic
This section assumes that the consuming project is a gradle project using a version catalog.

Ensure that the following is in the consuming project's `settings.gradle` file:
```groovy
pluginManagement {
    resolutionStrategy {
        eachPlugin {
            if (requested.id.toString() == "com.github.minirogue")
                useModule("com.github.minirogue:minirogue-build-logic:$requested.version")
        }
    }
    repositories {
        maven { url "https://jitpack.io" }
    }
}
```

Then add the following to the version catalog:
```toml
[plugins]
minirogue-plugin = { id = "com.github.minirogue", version = "0.3.2" }
``` 
and finally add the following to the root project `build.gradle`
```groovy
plugins {
    alias(libs.plugins.minirogue.plugin) apply false
}
```

### Applying and using the plugins
Then the following plugins may be used for any gradle modules contained in the project:
- `minirogue.multiplatform.library`
- `minirogue.test.app`
- `minirogue.android.app`
- `minirogue.jvm.app`

Then you may use the `minirogue` extension in the `build.gradle` file using the plugin to configure it:
```groovy
minirogue {
    android()
    ios()
    metro()
}
```

## Used by
I will add a list here of repos that I have which use the build logic defined in this repo once they are publicly viewable.

## Dependencies

This repo manages several dependencies (namely ones that have related gradle plugins) that will be inherited by any projects that consume it.
As of version 0.3.2 the following dependencies and versions are used:

- [Android Gradle Plugin (AGP)](https://developer.android.com/build/releases/gradle-plugin) = 9.2.1
- [ComposeMultiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/whats-new-compose-1610.html) = 1.11.1
- [Detekt](https://detekt.dev/changelog) = 2.0.0-alpha.5
- [HotReload](https://github.com/JetBrains/compose-hot-reload/releases) = 1.1.1
- [Java](https://www.java.com/releases/) = 21
- [Kotlin](https://kotlinlang.org/docs/releases.html#release-details) = 2.4.10
- [Kotlin Serialization](https://github.com/Kotlin/kotlinx.serialization/releases) = 1.11.0
- [Kotlin Symbol Processing](https://github.com/google/ksp/releases) = 2.3.9
- [Metro](https://github.com/ZacSweers/metro/releases) = 1.1.1
- [Room](https://developer.android.com/jetpack/androidx/releases/room) = 2.8.4

Minimum supported Gradle version: 9.4.1
Latest tested Gradle version: 9.5.0

Note: if using compose hot reload (compose desktop only), then also include the following in settings.gradle:
```
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.10.0"
}
```

## TODO
- Add docs for each plugin to detail their configurations
- Share the same config for this project's detekt and and the config that it uses for consuming libraries
- Set up some tests
- Figure out test tasks for CI config given JVM apps, Android Apps, multiplatform code, etc.
