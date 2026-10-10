package plugin

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.gradle.AppPlugin
import configuration.AddScriptsTaskConfiguration
import configuration.UniversalConfiguration
import configuration.applyUniversalConfigurations
import configuration.configureAndroidApp
import configuration.configureMetro
import ext.generateProjectNamespace
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty

public class TestAppPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply(AppPlugin::class.java)
            }
            val testAppExtension = extensions.create(
                "minirogue",
                MinirogueTestAppExtension::class.java,
                target,
            )

            applyUniversalConfigurations(
                testAppExtension.universalConfiguration,
            )
            configureAndroidApp()
            configureMetro()

            extensions.configure(ApplicationExtension::class.java) {
                defaultConfig {
                    applicationId = "com.${target.rootProject.name}.test.app"
                }
                namespace = generateProjectNamespace()
            }
        }
    }
}

public open class MinirogueTestAppExtension(private val project: Project) {
    // TODO replace with by-task DSL
    public val scriptsDirectory: DirectoryProperty = project.objects.directoryProperty()

    public fun metro(): Unit = project.configureMetro()

    internal val universalConfiguration = UniversalConfiguration(
        useGradleCheckerTask = false,
        addScriptsTaskConfiguration = AddScriptsTaskConfiguration(
            scriptsDirectory,
        ),
    )
}
