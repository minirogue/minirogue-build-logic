package task

import ext.generateProjectNamespace
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.OutputDirectories
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.UntrackedTask
import org.jetbrains.kotlin.konan.file.File
import javax.inject.Inject

internal enum class SourceType {
    SinglePlatform,
    CommonMultiplatform,
    AndroidMultiplatform,
    IosMultiplatform,
    JvmMultiplatform,
}

/**
 * A task to automatically generate directory structures for source code
 */
@UntrackedTask(
    because = "output directories of this task will not need to be recreated in general and will be changing regularly",
)
internal open class CreateSrcTask @Inject constructor(sourceType: SourceType) : DefaultTask() {
    private val pathToAdd = project.generateProjectNamespace().replace(
        ".",
        File.separator,
    )

    @OutputDirectories
    val outputDirectory = when (sourceType) {
        SourceType.SinglePlatform -> project.files(
            sourceDirectory("main"),
            sourceDirectory("test"),
        )

        SourceType.CommonMultiplatform -> project.files(
            sourceDirectory("commonMain"),
            sourceDirectory("commonTest"),
        )

        SourceType.AndroidMultiplatform -> project.files(
            sourceDirectory("androidMain"),
            sourceDirectory("androidHostTest"),
            sourceDirectory("androidDeviceTest"),
        )

        SourceType.JvmMultiplatform -> project.files(
            sourceDirectory("jvmMain"),
            sourceDirectory("jvmTest"),
        )

        SourceType.IosMultiplatform -> project.files(
            sourceDirectory("iosMain"),
            sourceDirectory("iosTest"),
        )
    }

    @TaskAction
    fun createDirectories() {
        // directories should be automatically created by the task at execution if they don't already exist
    }

    private fun sourceDirectory(source: String): String = "src${File.separator}" +
        "$source${File.separator}" +
        "kotlin${File.separator}" +
        pathToAdd
}
