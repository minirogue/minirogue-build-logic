package configuration

import dev.zacsweers.metro.gradle.MetroGradleSubplugin
import org.gradle.api.Project

internal fun Project.configureMetro() {
    plugins.apply(MetroGradleSubplugin::class.java)
}
