package task

import ext.customJson
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
@CacheableTask
internal abstract class ScoreboardTask : DefaultTask() {
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    val inputFiles: ConfigurableFileCollection = project.files()

    @TaskAction
    fun aggregateScorecards() {
        val scoreboard: MutableMap<String, Int> = mutableMapOf()
        for (inputFile in inputFiles) {
            if (inputFile.exists()) {
                val moduleScores = customJson.decodeFromString<Map<String, Int>>(
                    inputFile.readText(),
                )
                moduleScores.forEach { score ->
                    scoreboard.merge(score.key, score.value) { a, b ->
                        logger.lifecycle(
                            "adding score $b to ${score.key} from ${inputFile.path}",
                        )
                        a + b
                    }
                }
            }
        }
        logger.lifecycle("Scoreboard: $scoreboard")
    }
}
