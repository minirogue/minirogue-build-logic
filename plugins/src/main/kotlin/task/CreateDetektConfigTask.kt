package task

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import kotlin.jvm.java

@CacheableTask
internal open class CreateDetektConfigTask : DefaultTask() {
    @OutputFile // TODO make this configurable
    val outputFile = project.layout.buildDirectory.file(
        "minirogue/detekt-config.yml",
    )

    @TaskAction
    fun writeDetektConfig() {
        // TODO is this technically an input for the task?
        val inputStream = this::class.java.getResourceAsStream(
            "/detekt-config.yml",
        )
        requireNotNull(
            inputStream,
        ) { "detekt-config.yml not found in jar resources" }
        val outputStream = outputFile.get().asFile.outputStream()
        try {
            inputStream.copyTo(outputStream)
        } finally {
            inputStream.close()
            outputStream.close()
        }
    }
}
