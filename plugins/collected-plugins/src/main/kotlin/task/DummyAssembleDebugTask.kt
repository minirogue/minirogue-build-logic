package task

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.UntrackedTask

@UntrackedTask(because = "no inputs or outputs")
internal open class DummyAssembleDebugTask : DefaultTask() {
    @TaskAction
    fun doNothing() {
        // Task does nothing, a dependency on the standard assemble task is set up at configuration
    }
}
