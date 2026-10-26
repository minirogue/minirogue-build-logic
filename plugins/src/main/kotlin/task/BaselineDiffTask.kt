package task

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.UntrackedTask
import org.gradle.process.ExecOperations
import java.io.ByteArrayOutputStream
import javax.inject.Inject

@UntrackedTask(because = "Depends on git history at HEAD")
internal abstract class BaselineDiffTask : DefaultTask() {

    @get:OutputFile
    abstract val baselineDiffFile: RegularFileProperty

    @get:Inject
    abstract val execOperations: ExecOperations

    @TaskAction
    fun getBaselineDiff() {
        val outStream = ByteArrayOutputStream()
        // TODO make inputs configurable
        val maxCommitsToCheck = 10000
        val maxDaysToCheck = 30
        val mainBranch = "main"
        execOperations.exec {
            standardOutput = outStream
            commandLine(
                "git",
                "log",
                "--first-parent", mainBranch,
                "-n", maxCommitsToCheck,
                "--after", "$maxDaysToCheck days ago",
                "-p", "--", "lint-baseline.xml", "detekt-baseline.xml", // TODO configuration
            )
        }
        baselineDiffFile.get().asFile.also {
            outStream.writeTo(it.outputStream())
        }
    }
}
