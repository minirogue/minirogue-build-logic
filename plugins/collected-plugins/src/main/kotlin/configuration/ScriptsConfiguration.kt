package configuration

import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import task.AddScriptsTask
import task.MINIROGUE_TASK_GROUP
import java.io.File

private const val ADD_SCRIPTS_TASK = "addScripts"

internal data class AddScriptsTaskConfiguration(val configuredScriptsDirectory: DirectoryProperty)
internal fun Project.configureAddScriptsTask(configuration: AddScriptsTaskConfiguration) {
    if (rootProject.tasks.none { it.name == ADD_SCRIPTS_TASK }) {
        // Creates single instance of this task in root project
        rootProject.tasks.register(
            ADD_SCRIPTS_TASK,
            AddScriptsTask::class.java,
        ) {
            val configuredScriptsDirectory = configuration.configuredScriptsDirectory.convention(
                project.layout.projectDirectory.dir(
                    "scripts${File.separator}create-module",
                ),
            )
            scriptsDirectory.set(configuredScriptsDirectory)

            group = MINIROGUE_TASK_GROUP
            description = "Adds commonly useful scripts to the scripts directory."
        }
    }
}
