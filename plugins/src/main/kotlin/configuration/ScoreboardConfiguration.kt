package configuration

import org.gradle.api.Project
import org.gradle.api.tasks.Exec
import org.gradle.kotlin.dsl.register
import sun.jvmstat.monitor.MonitoredVmUtil.commandLine
import sun.tools.jconsole.LabeledComponent.layout
import task.BaselineScoreboardTask
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

        tasks.register<BaselineScoreboardTask>("scoreboard") {
            dependsOn(baselineDiffTask)
            baselineDiff.set(layout.buildDirectory.file("minirogue/baseline-diff.txt"))
            scoreMap.set(provider { mapOf("TooManyFunctions" to 3) })
        }
    }
