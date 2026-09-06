package task

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.UntrackedTask
import org.jetbrains.kotlin.konan.file.File

@UntrackedTask(
    because = "git process must be run to determine if it is actually up-to-date",
)
internal open class GetGitCommitNumberTask : DefaultTask() {
    @get:OutputFile
    val numberOfCommitsInBranchHistory = project.layout.buildDirectory
        .file("minirogue${File.separator}gitCommitNumber")

    @TaskAction
    fun getGitNumber() {
        logger.info("starting git command")
        ProcessBuilder("git", "rev-list", "--count", "HEAD")
            .redirectErrorStream(true)
            .redirectOutput(numberOfCommitsInBranchHistory.get().asFile)
            .start()
            .waitFor()
        // TODO error out of this if output is not an integer (will happen if error occurs)
    }
}
