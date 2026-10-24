package task

import ext.customJson
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction

internal abstract class ScoreboardTask : DefaultTask() {
    @InputFiles
    val inputFiles: ConfigurableFileCollection = project.files()

    @TaskAction
    fun aggregateScorecards() {
        val scoreboard: MutableMap<String, Int> = mutableMapOf()
        for (inputFile in inputFiles) {
            if (inputFile.exists()) {
                val moduleScores = customJson.decodeFromString<Map<String, Int>>(inputFile.readText())
                moduleScores.forEach { score ->
                    scoreboard.merge(score.key, score.value) { a, b -> a + b }
                }
            }
        }
        logger.lifecycle("Scoreboard: $scoreboard")
    }
}
