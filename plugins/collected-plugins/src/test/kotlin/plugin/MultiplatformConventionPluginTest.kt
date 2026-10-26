package plugin

import com.google.common.truth.Truth.assertThat
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.Parameter
import org.junit.jupiter.params.ParameterizedClass
import org.junit.jupiter.params.provider.EnumSource
import utils.GradleTestVersion
import java.io.File

@ParameterizedClass
@EnumSource(GradleTestVersion::class)
class MultiplatformConventionPluginTest {

    @Parameter
    lateinit var gradleVersion: GradleTestVersion

    @field:TempDir
    lateinit var testProjectDir: File

    private lateinit var settingsFile: File
    private lateinit var buildFile: File
    private lateinit var jvmFile: File
    private lateinit var commonFile: File
    private lateinit var androidFile: File

    @BeforeEach
    fun setup() {
        settingsFile = File(testProjectDir, "settings.gradle")
        buildFile = File(testProjectDir, "build.gradle")
        jvmFile = File(
            testProjectDir,
            "src/jvmMain/kotlin/somepackage/TestJavaClass.kt",
        ).also { it.parentFile.mkdirs() }
        androidFile = File(
            testProjectDir,
            "src/androidMain/kotlin/somepackage/TestAndroidClass.kt",
        ).also { it.parentFile.mkdirs() }
        commonFile = File(
            testProjectDir,
            "src/commonMain/kotlin/somepackage/TestCommonClass.kt",
        ).also { it.parentFile.mkdirs() }
    }

    @AfterEach
    fun tearDown() {
        // clear build cache
        File(testProjectDir, "build").deleteRecursively()
    }

    @Test
    fun `can assemble jvm only`() {
        // Arrange
        writeBuildFile(jvm = true, android = false)
        writeSettingsFile()
        writeJavaFile()
        writeCommonFile()

        // Act
        val result = runBuild(listOf("assemble"))

        // Assert
        assertThat(
            result.task(":assemble")?.outcome,
        ).isEqualTo(TaskOutcome.SUCCESS)
    }

    @Test
    fun `can assemble android only`() {
        // Arrange
        writeBuildFile(jvm = false, android = true)
        writeSettingsFile()
        writeJavaFile()
        writeCommonFile()

        // Act
        val result = runBuild(listOf("assemble"))

        // Assert
        assertThat(
            result.task(":assemble")?.outcome,
        ).isEqualTo(TaskOutcome.SUCCESS)
    }

    @Test
    fun `can assemble jvm and android only`() {
        // Arrange
        writeBuildFile(jvm = true, android = true)
        writeSettingsFile()
        writeJavaFile()
        writeAndroidFile()
        writeCommonFile()

        // Act
        val result = runBuild(listOf("assemble"))

        // Assert
        assertThat(
            result.task(":assemble")?.outcome,
        ).isEqualTo(TaskOutcome.SUCCESS)
    }

    private fun runBuild(arguments: List<String>): BuildResult = GradleRunner.create()
        .withPluginClasspath()
        .withGradleVersion(gradleVersion.version)
        .withProjectDir(testProjectDir)
        .withArguments(arguments)
        .build()

    private fun writeFile(destination: File, content: String) {
        destination.bufferedWriter().use { writer ->
            writer.write(content)
        }
    }

    private fun writeSettingsFile(projectName: String = "testproject") = writeFile(
        settingsFile,
        """
            dependencyResolutionManagement {
                repositories {
                    mavenCentral()
                    google()
                }
            }
            
            rootProject.name = "$projectName" 
            
            buildCache {
                local.directory = "$LOCAL_BUILD_CACHE_DIRECTORY"
            }
        """.trimMargin(),
    )

    private fun writeBuildFile(
        implementationDependencies: List<String> = emptyList(),
        compileOnlyDependencies: List<String> = emptyList(),
        testImplementationDependencies: List<String> = emptyList(),
        testCompileOnlyDependencies: List<String> = emptyList(),
        apiDependencies: List<String> = emptyList(),
        jvm: Boolean = false,
        android: Boolean = false,
    ) = writeFile(
        buildFile,
        """
            plugins {
                id("$PLUGIN_NAME")
            }
            
            ${getExtensionBlock(jvm, android)}

            dependencies {
                ${implementationDependencies.joinToString(
            separator = "\r\n",
        ) { "implementation(\"$it\")" }}
                ${apiDependencies.joinToString(
            separator = "\r\n",
        ) { "api(\"$it\")" }}
                ${compileOnlyDependencies.joinToString(
            separator = "\r\n",
        ) { "compileOnly(\"$it\")" }}
                ${testImplementationDependencies.joinToString(
            separator = "\r\n",
        ) { "testImplementation(\"$it\")" }}
                ${testCompileOnlyDependencies.joinToString(
            separator = "\r\n",
        ) { "testCompileOnly(\"$it\")" }}
            }
        """.trimIndent(),
    )

    private fun getExtensionBlock(jvm: Boolean = false, android: Boolean = false) =
        """
        minirogue {
            platforms {
                ${if (jvm) "jvm()" else ""}
                ${if (android) "android()" else ""}
            }
        }
        """.trimIndent()

    private fun writeCommonFile() = writeFile(
        commonFile,
        """
            package somepackage
            
            data class TestCommonClass(val someVal: String)
            
        """.trimIndent(),
    )

    private fun writeJavaFile() = writeFile(
        jvmFile,
        """
            package somepackage
            
            data class TestJavaClass(val someVal: String)
            
        """.trimIndent(),
    )

    private fun writeAndroidFile() = writeFile(
        commonFile,
        """
            package somepackage
            
            data class TestAndroidClass(val someVal: String)
            
        """.trimIndent(),
    )

    companion object {
        private const val LOCAL_BUILD_CACHE_DIRECTORY = "build-cache"
        private const val PLUGIN_NAME = "minirogue.multiplatform.library"
    }
}
