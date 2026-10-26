package task

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.UntrackedTask

@UntrackedTask(because = "rarely run task that doesn't need optimizations")
internal abstract class AddScriptsTask : DefaultTask() {

    @get:OutputDirectory
    abstract val scriptsDirectory: DirectoryProperty

    @TaskAction
    fun copyScripts() {
        copyModuleCreationScript()
    }

    private fun copyModuleCreationScript() {
        listOf(
            "/scripts/create-module/createModule.main.kts",
            "/scripts/create-module/internal-template.gradle.kts",
            "/scripts/create-module/public-template.gradle.kts",
        ).forEach { resourcePath ->
            val inputStream = this::class.java.getResourceAsStream(resourcePath)
            requireNotNull(
                inputStream,
            ) { "$resourcePath not found in jar resources" }
            val outputStream = scriptsDirectory.get()
                .file(resourcePath.split("/").last())
                .asFile
                .outputStream()
            try {
                inputStream.copyTo(outputStream)
            } finally {
                inputStream.close()
                outputStream.close()
            }
        }
    }
}
