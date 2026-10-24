package configuration

import org.gradle.api.Project
import org.gradle.api.tasks.Exec
import org.gradle.kotlin.dsl.register
import org.jetbrains.compose.internal.utils.registerOrConfigure
import task.ScoreboardTask
import task.ScorecardTask
import java.io.ByteArrayOutputStream

internal fun Project.configureScoreboardTask() {
    val baselineDiffFile = layout.buildDirectory.file("minirogue/baseline-diff.txt")
    // TODO why do the inputs of Exec really not use Provider by now?
    baselineDiffFile.get().asFile.parentFile.mkdirs()
    baselineDiffFile.get().asFile.createNewFile()
    val baselineDiffTask = tasks.register<Exec>("baselineDiff") {
        val outStream = ByteArrayOutputStream()
        // TODO make configurable
        val maxCommitsToCheck = 10000
        val maxDaysToCheck = 30
        outputs.file(baselineDiffFile)
        standardOutput = outStream
        commandLine(
            "git",
            "log",
            "-n", maxCommitsToCheck,
            "--after", "$maxDaysToCheck days ago",
            "-p", "--", "lint-baseline.xml", "detekt-baseline.xml" // TODO configuration
        )
        doLast {
            baselineDiffFile.get().asFile.also {
                if (!it.exists()) {
                    it.parentFile.mkdirs()
                    it.createNewFile()
                    outStream.writeTo(it.outputStream())
                }
            }
        }
    }

    val scorecardTask = tasks.register<ScorecardTask>("scorecard") {
        dependsOn(baselineDiffTask)
        baselineDiff.set(layout.buildDirectory.file("minirogue/baseline-diff.txt"))
        outputFile.set(layout.buildDirectory.file("minirogue/scorecard.json"))
        scoreMap.set(provider { mapOf("TooManyFunctions" to 3) })
    }
    rootProject.tasks.registerOrConfigure<ScoreboardTask>("scoreboard") {
        inputFiles.builtBy(scorecardTask)
        inputFiles.from(scorecardTask.map { it.outputFile.get() })
        dependsOn(scorecardTask)
    }
}
