package task

import ext.MINIROGUE_PROBLEM_GROUP
import ext.customJson
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.problems.ProblemId
import org.gradle.api.problems.Problems
import org.gradle.api.provider.MapProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

// TODO include warning scoring as well
@CacheableTask
internal abstract class ScorecardTask : DefaultTask() {

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val baselineDiff: RegularFileProperty

    @get:Input
    abstract val scoreMap: MapProperty<String, Int>

    @get:Input
    val modulePath: String = project.path

    @get:OutputFile
    val outputFile: RegularFileProperty = project.objects.fileProperty()

    @get:Inject
    abstract val problems: Problems

    @TaskAction
    fun tallyScores() {
        val actualScoreMap = scoreMap.get()
        var currentAuthor = ""
        val scorecard: MutableMap<String, Int> = mutableMapOf()
        baselineDiff.asFile.get().bufferedReader().useLines { lines ->
            lines.forEach { line ->
                when {
                    line.startsWith(
                        "Author: ",
                    ) -> currentAuthor = line.substringAfter("Author: ").trim()

                    line.startsWith(
                        "-",
                    ) && line.isIssueLine() -> scorecard.merge(
                        currentAuthor,
                        actualScoreMap.score(line),
                    ) { prevScore, lineScore -> prevScore + lineScore }

                    line.startsWith(
                        "+",
                    ) && line.isIssueLine() -> scorecard.merge(
                        currentAuthor,
                        actualScoreMap.score(line),
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
        return 1.also {
            problems.reporter.report(MISSING_SCOREBOARD_MAPPING_ID) {
                details("No point mapping found for baselined issue: $line")
                solution(
                    "Add issue to scoreMap for the scoreboard task configuration in minirogue-build-tools",
                )
//                documentedAt("https://gradle.org/hello-problems#recipient") // TODO
                stackLocation()
            }
            logger.lifecycle("w: missing baseline score mapping for\n$line")
        }
    }

    private fun String.isIssueLine(): Boolean = contains(
        "<ID>",
    ) || contains("id=\"")

    companion object {
        private val MISSING_SCOREBOARD_MAPPING_ID = ProblemId.create(
            "missing-scoreboard-mapping",
            "A scoreboard mapping is missing",
            MINIROGUE_PROBLEM_GROUP,
        )
    }
}
