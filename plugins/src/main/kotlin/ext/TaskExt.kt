package ext

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import task.MINIROGUE_CHECK_TASK

// Credit to org.jetbrains.compose.internal.utils
internal inline fun <reified T : Task> TaskContainer.registerOrConfigure(
    taskName: String,
    crossinline configureFn: T.() -> Unit,
): TaskProvider<T> = when (taskName) {
    in names -> named<T>(taskName)
    else -> register<T>(taskName)
}.apply {
    configure { configureFn() }
}

context(project: Project)
internal fun <T : Task> TaskProvider<T>.addToMinirogueCheck() {
    project.tasks.named(
        MINIROGUE_CHECK_TASK,
    ) { dependsOn(this@addToMinirogueCheck) }
}
