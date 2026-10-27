package configuration

import org.gradle.api.Project
import task.ClearEmptyDirectoryTask
import task.CreateSrcTask
import task.MINIROGUE_TASK_GROUP
import task.SourceType

internal const val CLEAR_EMPTY_DIRS_TASK_NAME = "clearEmptyDirs"
internal const val CREATE_SRC_TASK_NAME = "createSrc"
internal fun Project.configureCreateSrc(srcType: SourceType) {
    val clearEmptyDirsTask = rootProject.tasks.findByName(
        CLEAR_EMPTY_DIRS_TASK_NAME,
    ) ?: rootProject.tasks.register(
        CLEAR_EMPTY_DIRS_TASK_NAME,
        ClearEmptyDirectoryTask::class.java,
    ) {
        group = MINIROGUE_TASK_GROUP
        description = "Recursively clears all empty directories"
    }
    tasks.named("clean").configure { finalizedBy(clearEmptyDirsTask) }
    val rootTask = tasks.findByName(
        CREATE_SRC_TASK_NAME,
    ) ?: tasks.register(CREATE_SRC_TASK_NAME).get()
    val newTask = tasks.register(
        "$CREATE_SRC_TASK_NAME$srcType",
        CreateSrcTask::class.java,
        srcType,
    )
        .apply {
            configure {
                group = MINIROGUE_TASK_GROUP
                description = "Creates source directories consistent with module structure"
            }
        }
    rootTask.dependsOn(newTask)
}
