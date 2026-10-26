package configuration

import org.gradle.api.Project
import org.gradle.api.plugins.JavaApplication

internal fun Project.configureJvmApp(mainClass: String) {
    pluginManager.apply("application")
    extensions.getByType(JavaApplication::class.java).apply {
        getMainClass().set(mainClass)
    }
}
