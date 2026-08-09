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

## TODO
- Add docs for each plugin to detail their configurations
- Share the same config for this project's detekt and and the config that it uses for consuming libraries
- Set up some tests
- Figure out test tasks for CI config given JVM apps, Android Apps, multiplatform code, etc.
