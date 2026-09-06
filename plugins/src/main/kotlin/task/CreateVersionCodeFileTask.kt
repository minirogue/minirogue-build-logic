package task

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

// TODO determine if cacheability is actually beneficial here. I suspect not.
// TODO add tests
@CacheableTask
internal abstract class CreateVersionCodeFileTask : DefaultTask() {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val versionCodeSource: RegularFileProperty

    @get:OutputFile
    val outputFile = project.layout.buildDirectory.file("minirogue/versionCode")

    @TaskAction
    fun copyScripts() {
        versionCodeSource.get().asFile.copyTo(
            target = outputFile.get().asFile,
            overwrite = true,
        )
    }
}
