package configuration

import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.jvm.toolchain.JavaLanguageVersion
import versions.JAVA_VERSION

public fun Project.configureJvm() {
    extensions.configure(JavaPluginExtension::class.java) {
        toolchain { languageVersion.set(JavaLanguageVersion.of(JAVA_VERSION)) }
    }
}
