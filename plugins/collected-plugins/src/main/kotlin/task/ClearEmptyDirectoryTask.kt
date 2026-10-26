package task

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.UntrackedTask

/**
 * Recursively clear all empty directories
 */
@UntrackedTask(because = "no specific outputs, this is just a utility task")
internal open class ClearEmptyDirectoryTask : DefaultTask() {

    @TaskAction
    fun clearEmptyDirectories() {
        Runtime.getRuntime().exec("find . -type d -empty -delete")
    }
}
