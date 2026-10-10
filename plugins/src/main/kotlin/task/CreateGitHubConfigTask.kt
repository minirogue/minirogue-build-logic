package task

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.UntrackedTask
import org.jetbrains.kotlin.konan.file.File
import javax.inject.Inject

@UntrackedTask(because = "rarely run task that doesn't need optimizations")
internal open class CreateGitHubConfigTask @Inject constructor() : DefaultTask() {

    @OutputFile
    val dependabotYamlFile = project.rootProject.file(
        ".github${File.separator}dependabot.yml",
    )

    @OutputFile
    val buildAndChecksWorkflowFile =
        project.rootProject.file(
            ".github${File.separator}workflows${File.separator}checks.yml",
        )

    @TaskAction
    fun createFiles() {
        createStandardBuildWorkflow()
        createDependabotYaml()
    }

    // TODO change to use resource like detekt and scripts tasks
    private fun createStandardBuildWorkflow() {
        buildAndChecksWorkflowFile.printWriter().use { out ->
            out.apply {
                println("name: Build, Analyze, and Test")
                println("on:")
                println("  push:")
                println("    branches: [ \"main\" ]")
                println("  pull_request:")
                println("    branches: [ \"main\" ]")
                println("jobs:")
                println("  build:")
                println("    runs-on: ubuntu-latest")
                println("    permissions:")
                println("      contents: read")
                println("    steps:")
                println("      - uses: actions/checkout@v4")
                println("      - name: Set up JDK 21")
                println("        uses: actions/setup-java@v4")
                println("        with:")
                println("          java-version: '21'")
                println("          distribution: 'temurin'")
                println("      - name: Setup Gradle")
                println("        uses: gradle/actions/setup-gradle@v5")
                println("        with:")
                println("          build-scan-publish: true")
                println(
                    "          build-scan-terms-of-use-url: 'https://gradle.com/terms-of-service'",
                )
                println("          build-scan-terms-of-use-agree: 'yes'")
                println("        run: ./gradlew ciChecks --continue ")
            }
        }
    }

    private fun createDependabotYaml() {
        dependabotYamlFile.printWriter().use { out ->
            out.println(
                """
                version: 2
                updates:
                  - package-ecosystem: "gradle" # See documentation for possible values
                    directory: "/" # Location of package manifests
                    schedule:
                      interval: "daily"
                    target-branch: "main"
                    reviewers:
                      - Minirogue
                """.trimIndent(),
            )
        }
    }
}
