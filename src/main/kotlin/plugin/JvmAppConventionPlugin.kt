package plugin

import configuration.AddScriptsTaskConfiguration
import configuration.UniversalConfiguration
import configuration.applyUniversalConfigurations
import configuration.configureCompose
import configuration.configureJvm
import configuration.configureJvmApp
import configuration.configureMetro
import configuration.configureSerialization
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import task.CI_CHECK_TASK

public class JvmAppConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("org.jetbrains.kotlin.jvm")
            }

            val jvmAppExtension = extensions.create(
                "minirogue",
                MinirogueJvmAppExtension::class.java,
                target,
            )

            applyUniversalConfigurations(jvmAppExtension.universalConfiguration)
            configureJvm()
            tasks.named(CI_CHECK_TASK) {
                dependsOn("assemble")
                dependsOn("test")
            }
        }
    }
}

public open class MinirogueJvmAppExtension(private val project: Project) {
    // TODO replace with by-task DSL
    public val scriptsDirectory: DirectoryProperty = project.objects.directoryProperty()

    internal val universalConfiguration = UniversalConfiguration(
        useGradleCheckerTask = false,
        addScriptsTaskConfiguration = AddScriptsTaskConfiguration(
            scriptsDirectory,
        ),
    )

    public fun composeApp(mainClass: String, useHotReload: Boolean = false): Unit = project.configureCompose(
        desktopMainClass = mainClass,
        useHotReload = useHotReload,
    )

    public fun jvmApp(mainClass: String): Unit = project.configureJvmApp(
        mainClass,
    )
    public fun serialization(): Unit = project.configureSerialization()
    public fun metro(): Unit = project.configureMetro()
}
