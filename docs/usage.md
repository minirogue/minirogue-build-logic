## Usage

### Importing the build logic

This section assumes that the consuming project is a gradle project using a version catalog.

Ensure that the following is in the consuming project's `settings.gradle.kts` file:

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
minirogue-plugin = { id = "com.github.minirogue", version = "{{ version_name }}" }
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
