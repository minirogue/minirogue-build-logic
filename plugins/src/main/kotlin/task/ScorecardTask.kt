package task

import ext.customJson
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

internal abstract class ScorecardTask : DefaultTask() {

    @get:InputFile
    abstract val baselineDiff: RegularFileProperty

    @get:Input
    abstract val scoreMap: MapProperty<String, Int>

    @get:Input
    val modulePath: String = project.path

    @get:OutputFile
     val outputFile: RegularFileProperty = project.objects.fileProperty()


    @TaskAction
    fun tallyScores() {
        val actualScoreMap = scoreMap.get()
        var currentAuthor = ""
        val scorecard: MutableMap<String, Int> = mutableMapOf()
        baselineDiff.asFile.get().bufferedReader().useLines { lines ->
            lines.forEach { line ->
                when {
                    line.startsWith("Author: ") -> currentAuthor = line.substringAfter("Author: ").trim()
                    line.startsWith("-") && line.isIssueLine() -> scorecard.merge(
                        currentAuthor,
                        actualScoreMap.score(line)
                    ) { prevScore, lineScore -> prevScore + lineScore }

                    line.startsWith("+") && line.isIssueLine() -> scorecard.merge(
                        currentAuthor,
                        actualScoreMap.score(line)
                    ) { prevScore, lineScore -> prevScore - lineScore }
                }
            }
        }
        outputFile.asFile.get().writeText(customJson.encodeToString(scorecard))
    }

    private fun Map<String, Int>.score(line: String): Int {
        entries.forEach {
            if (line.contains(it.key)) return it.value
        }
        return 1.also { logger.warn("w: missing scoreboard mapping for\n$line") }
    }

    private fun String.isIssueLine(): Boolean = contains("<ID>") || contains("id=\"")

}
