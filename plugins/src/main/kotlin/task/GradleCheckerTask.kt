package task

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskExecutionException
import java.io.File
@CacheableTask
internal open class GradleCheckerTask : DefaultTask() {
    private val acceptableRoots = listOf(
        "plugins",
        "minirogue",
        "kotlin.sourceSets.androidMain.dependencies",
        "kotlin.sourceSets.androidUnitTest.dependencies",
        "kotlin.sourceSets.androidInstrumentedTest.dependencies",
        "kotlin.sourceSets.commonMain.dependencies",
        "dependencies",
        "}",
    )

    @InputFile
    @PathSensitive(value = PathSensitivity.RELATIVE)
    val gradleFile: File = project.buildFile

    @Suppress("MagicNumber")
    @TaskAction
    fun checkGradle() {
        val allLines = gradleFile.readLines()
        checkRootLines(
            allLines.filter { it.firstOrNull()?.isWhitespace() == false },
        )
        checkPluginsLines(allLines.take(3))
    }

    private fun checkPluginsLines(pluginsLines: List<String>) {
        assert(pluginsLines[0] == "plugins {") {
            "first line of build.gradle must be \"plugins {\": ${pluginsLines[0]}"
        }
        assert(pluginsLines[1].contains("minirogue.")) {
            "Only minirogue plugins are allowed in the plugin block: ${pluginsLines[1].trim()}"
        }
        assert(pluginsLines[2] == "}") {
            "plugins block should only contain one (minirogue plugin) and end with \"}\": ${pluginsLines[2]}"
        }
    }

    private fun checkRootLines(rootLines: List<String>) {
        rootLines.forEach { root ->
            var isRootAcceptable = false
            acceptableRoots.forEach {
                // root is acceptable if it is in our allowlist
                isRootAcceptable = isRootAcceptable || root.startsWith(it)
            }
            if (!isRootAcceptable) {
                throw TaskExecutionException(
                    this,
                    AssertionError(
                        "All build.gradle lines must start with whitespace or one of $acceptableRoots\n" +
                            "\"$root\" does not fit this paradigm",
                    ),
                )
            }
        }
    }
}
