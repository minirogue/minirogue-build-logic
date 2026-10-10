package configuration

import org.gradle.api.Project
import task.CreateGitHubConfigTask
import task.MINIROGUE_TASK_GROUP

private const val CREATE_GITHUB_CONFIG_TASK = "createGitHubConfig"

internal fun Project.configureGitHubConfigTask() {
    // Creates single instance of this task in root project
    if (rootProject.tasks.none { it.name == CREATE_GITHUB_CONFIG_TASK }) {
        rootProject.tasks.register(
            CREATE_GITHUB_CONFIG_TASK,
            CreateGitHubConfigTask::class.java,
        ) {
            group = MINIROGUE_TASK_GROUP
            description =
                "Generates a github actions workflow to automatically run all common checks"
        }
    }
}
