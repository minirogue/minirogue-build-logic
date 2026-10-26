package configuration

import ext.registerOrConfigure
import org.gradle.api.Project
import org.gradle.kotlin.dsl.register
import task.BaselineDiffTask
import task.ScoreboardTask
import task.ScorecardTask

internal fun Project.configureScoreboardTask() {
    val baselineDiffTask = tasks.register<BaselineDiffTask>("baselineDiff") {
        baselineDiffFile.set(
            layout.buildDirectory.file("minirogue/baseline-diff.txt")
        )
    }

    val scorecardTask = tasks.register<ScorecardTask>("scorecard") {
        dependsOn(baselineDiffTask)
        baselineDiff.set(baselineDiffTask.flatMap { it.baselineDiffFile })
        outputFile.set(layout.buildDirectory.file("minirogue/scorecard.json"))
        scoreMap.set(
            provider {
                mapOf(
                    "TooManyFunctions" to 1,
                    "ConstructorParameterNaming" to 1,
                    "MagicNumber" to 1,
                    "FunctionNaming" to 1,
                    "LongMethod" to 1,
                    "MaximumLineLength" to 1,
                    "CyclomaticComplexMethod" to 1,
                )
            },
        )
    }
    rootProject.tasks.registerOrConfigure<ScoreboardTask>("scoreboard") {
        inputFiles.builtBy(scorecardTask)
        inputFiles.from(scorecardTask.map { it.outputFile.get() })
        dependsOn(scorecardTask)
    }
}
