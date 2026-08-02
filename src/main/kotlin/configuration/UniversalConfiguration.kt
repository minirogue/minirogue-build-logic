package configuration

import ext.isMultiplatform
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask
import task.GradleCheckerTask
import task.MINIROGUE_CHECK_TASK
import task.MINIROGUE_TASK_GROUP
import task.SourceType

internal data class UniversalConfiguration(
    val useGradleCheckerTask: Boolean = true,
    val addScriptsTaskConfiguration: AddScriptsTaskConfiguration,
)

internal fun Project.applyUniversalConfigurations(universalConfiguration: UniversalConfiguration) {
    createMinirogueCheckTask()
    if (universalConfiguration.useGradleCheckerTask) configureGradleChecker()
    configureDetekt()
    configureGitHubConfigTask()
    configureCreateSrc(
        if (isMultiplatform()) SourceType.CommonMultiplatform else SourceType.SinglePlatform,
    )
    configureAddScriptsTask(universalConfiguration.addScriptsTaskConfiguration)
    tasks.withType(KotlinCompilationTask::class.java) {
        compilerOptions {
            freeCompilerArgs.add("-Xcontext-parameters")
            freeCompilerArgs.add("-Xexpect-actual-classes")
        }
    }
}

private fun Project.createMinirogueCheckTask() {
    tasks.register(MINIROGUE_CHECK_TASK) {
        group = MINIROGUE_TASK_GROUP
        description = "Runs standard non-test checks for this module, e.g. detekt, lint, etc."
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
