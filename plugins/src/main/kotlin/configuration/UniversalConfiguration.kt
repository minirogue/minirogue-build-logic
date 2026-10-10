package configuration

import ext.isMultiplatform
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.HasConfigurableKotlinCompilerOptions
import task.CI_CHECK_TASK
import task.GradleCheckerTask
import task.MINIROGUE_CHECK_TASK
import task.MINIROGUE_TASK_GROUP
import task.SourceType

internal data class UniversalConfiguration(
    val useGradleCheckerTask: Boolean = true,
    val addScriptsTaskConfiguration: AddScriptsTaskConfiguration,
)

internal fun Project.applyUniversalConfigurations(universalConfiguration: UniversalConfiguration) {
    createCustomAggregateTasks()
    if (universalConfiguration.useGradleCheckerTask) configureGradleChecker()
    configureDetekt()
    configureGitHubConfigTask()
    configureCreateSrc(
        if (isMultiplatform()) SourceType.CommonMultiplatform else SourceType.SinglePlatform,
    )
    configureAddScriptsTask(universalConfiguration.addScriptsTaskConfiguration)
    extensions.configure(HasConfigurableKotlinCompilerOptions::class.java) {
        compilerOptions {
            freeCompilerArgs.add("-Xexpect-actual-classes")
        }
    }
}

private fun Project.createCustomAggregateTasks() {
    tasks.register(MINIROGUE_CHECK_TASK) {
        group = MINIROGUE_TASK_GROUP
        description = "Runs standard non-test checks for this module, e.g. detekt, lint, etc."
    }
    tasks.register(CI_CHECK_TASK) {
        group = MINIROGUE_TASK_GROUP
        description = "Runs all standard CI checks"
        dependsOn(MINIROGUE_CHECK_TASK)
    }
}

private fun Project.configureGradleChecker() {
    val gradleCheckerTaskProvider =
        tasks.register("checkGradleConfig", GradleCheckerTask::class.java) {
            group = MINIROGUE_TASK_GROUP
            description =
                "Checks the gradle file to ensure it follows a \"3-block\" format with only one plugin"
        }
    tasks.named(MINIROGUE_CHECK_TASK) { dependsOn(gradleCheckerTaskProvider) }
}
