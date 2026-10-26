package configuration

import dev.detekt.gradle.Detekt
import dev.detekt.gradle.extensions.DetektExtension
import dev.detekt.gradle.plugin.DetektPlugin
import ext.addToMinirogueCheck
import ext.registerOrConfigure
import org.gradle.api.Project
import task.CreateDetektConfigTask
import task.MINIROGUE_TASK_GROUP
import versions.DETEKT_VERSION

private const val CREATE_DETEKT_CONFIG_TASK = "createDetektConfig"

internal fun Project.configureDetekt() {
    plugins.apply(DetektPlugin::class.java)
    // Add formatting, which is a wrapper around ktlint
    dependencies.add(
        "detektPlugins",
        "dev.detekt:detekt-rules-ktlint-wrapper:$DETEKT_VERSION",
    )

    val rootDetektConfigTask =
        rootProject.tasks.registerOrConfigure<CreateDetektConfigTask>(
            CREATE_DETEKT_CONFIG_TASK,
        ) {
            group = MINIROGUE_TASK_GROUP
            description = "Creates a common detekt configuration file defined by convention plugin"
        }

    tasks.withType(Detekt::class.java) {
        dependsOn(rootDetektConfigTask)
    }
    tasks.named("detekt").addToMinirogueCheck()

    extensions.configure(DetektExtension::class.java) {
        source.setFrom(
            "src/main",
            "src/androidMain", "src/androidUnitTest", "src/androidInstrumentedTest",
            "src/commonMain", "src/commonTest",
            "src/jvmMain", "src/jvmTest",
            "src/iosMain", "src/iosTest",
        )
        config.setFrom(
            files(rootProject.tasks.getByName(CREATE_DETEKT_CONFIG_TASK)),
        )
        buildUponDefaultConfig.set(true)
        autoCorrect.set(true)
    }
}
