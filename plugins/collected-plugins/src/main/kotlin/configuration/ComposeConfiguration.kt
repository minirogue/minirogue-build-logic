package configuration

import ext.isMultiplatform
import org.gradle.api.Project
import org.jetbrains.compose.ComposeExtension
import org.jetbrains.compose.ComposePlugin
import org.jetbrains.compose.desktop.DesktopExtension
import org.jetbrains.compose.reload.gradle.ComposeHotReloadPlugin
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradleSubplugin
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import versions.COMPOSEMATERIAL3_VERSION
import versions.COMPOSEMULTIPLATFORM_VERSION

internal fun Project.configureCompose(desktopMainClass: String? = null, useHotReload: Boolean = false) {
    pluginManager.apply(ComposeCompilerGradleSubplugin::class.java)
    pluginManager.apply(ComposePlugin::class.java)
    if (useHotReload) pluginManager.apply(ComposeHotReloadPlugin::class.java)

    if (isMultiplatform()) {
        kotlinExtension.sourceSets.named("commonMain") {
            dependencies {
                implementation(
                    "org.jetbrains.compose.foundation:foundation:$COMPOSEMULTIPLATFORM_VERSION",
                )
                implementation(
                    "org.jetbrains.compose.material3:material3:$COMPOSEMATERIAL3_VERSION",
                )
                implementation(
                    "org.jetbrains.compose.runtime:runtime:$COMPOSEMULTIPLATFORM_VERSION",
                )
                implementation(
                    "org.jetbrains.compose.components:components-resources:$COMPOSEMULTIPLATFORM_VERSION",
                )
            }
        }
    } else {
        dependencies.apply {
            add(
                "implementation",
                "org.jetbrains.compose.foundation:foundation:$COMPOSEMULTIPLATFORM_VERSION",
            )
            add(
                "implementation",
                "org.jetbrains.compose.material3:material3:$COMPOSEMATERIAL3_VERSION",
            )
            add(
                "implementation",
                "org.jetbrains.compose.runtime:runtime:$COMPOSEMULTIPLATFORM_VERSION",
            )
            add(
                "implementation",
                "org.jetbrains.compose.components:components-resources:$COMPOSEMULTIPLATFORM_VERSION",
            )
        }
    }

    if (desktopMainClass != null) {
        dependencies.add(
            "implementation",
            ComposePlugin.DesktopDependencies.currentOs,
        )
        extensions.getByType(ComposeExtension::class.java).extensions
            .getByType(
                DesktopExtension::class.java,
            ).application.mainClass = desktopMainClass
    }
}
