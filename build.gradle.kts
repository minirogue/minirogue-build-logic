import org.gradle.kotlin.dsl.`kotlin-dsl`
import org.gradle.kotlin.dsl.`maven-publish`
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.File

plugins {
    `kotlin-dsl`
    `maven-publish`
    alias(libs.plugins.gradle.publish)
    alias(libs.plugins.detekt)
}

group = "org.minirogue"
version = "0.3.3"

kotlin {
    explicitApiWarning()
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-parameters")
    }
}

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

java {
    sourceCompatibility = JavaVersion.toVersion(libs.versions.java.get())
    targetCompatibility = JavaVersion.toVersion(libs.versions.java.get())
}

tasks.test {
    useJUnitPlatform()
}

detekt {
    config.setFrom(files("src/main/resources/detekt-config.yml"))
    buildUponDefaultConfig = true
    autoCorrect = true
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testImplementation(gradleTestKit())
    testImplementation(libs.truth)
    testImplementation(platform("org.junit:junit-bom:6.1.0"))
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    implementation(libs.android.gradlePlugin)
    implementation(libs.compose.compilerGradlePlugin)
    implementation(libs.compose.gradlePlugin)
    implementation(libs.compose.hotReloadGradlePlugin)
    implementation(libs.detekt.gradlePlugin)
    implementation(libs.develocity.gradlePlugin)
    implementation(libs.foojayResolver.gradlePlugin)
    implementation(libs.kotlin.gradlePlugin)
    implementation(libs.ksp.gradlePlugin)
    implementation(libs.metro.gradlePlugin)
    implementation(libs.room.gradlePlugin)
    implementation(libs.serialization.gradlePlugin)

    detektPlugins(libs.detekt.formatting)
}

gradlePlugin {
    plugins {
        register("multiplatformLibrary") {
            id = "minirogue.multiplatform.library"
            implementationClass = "plugin.KotlinMultiplatformLibraryConventionPlugin"
        }
        register("testApp") {
            id = "minirogue.test.app"
            implementationClass = "plugin.TestAppPlugin"
        }
        register("androidApp") {
            id = "minirogue.android.app"
            implementationClass = "plugin.AndroidAppConventionPlugin"
        }
        register("jvmApp") {
            id = "minirogue.jvm.app"
            implementationClass = "plugin.JvmAppConventionPlugin"
        }
        register("settings") {
            id = "minirogue.settings"
            implementationClass = "plugin.SettingsConventionPlugin"
        }
    }
}

// Add versions to this library's source code
val generatedVersionSourceDir =
    layout.buildDirectory.dir("generated${File.separator}source${File.separator}versions")
val versionDocFile = rootProject.file("docs/dependencies.md")
tasks.register("generatePluginVersionSource").configure {
    val versionCatalogFile =
        layout.projectDirectory.file("gradle${File.separator}libs.versions.toml")
    inputs.file(versionCatalogFile)
    outputs.dir(generatedVersionSourceDir)
    inputs.file(versionDocFile)
    outputs.file(versionDocFile)
    doLast {
        val versionLines = extractVersionLines(versionCatalogFile.asFile)
        generatedVersionSourceDir.get().file("Versions.kt").asFile.apply {
            parentFile.mkdirs()
            writeText(
                buildString {
                    appendLine("// Generated file. Do not edit!")
                    appendLine("package versions")
                    appendLine()
                    versionLines.forEach { appendLine(convertVersionLineToKotlinVersionLine(it)) }
                }
            )
        }
        val versionDocLines = versionDocFile.readLines()
        versionDocFile.writeText(buildString {
            versionDocLines.forEach { readmeLine ->
                appendLine(
                    if (readmeLine.startsWith("-") && readmeLine.contains("=")) {
                        updateReadmeVersionLine(readmeLine, versionLines)
                    } else readmeLine
                )
            }
        })
    }
}
sourceSets["main"].kotlin { srcDir(generatedVersionSourceDir) }
tasks.withType<KotlinCompile>().configureEach { dependsOn("generatePluginVersionSource") }
tasks.withType<Jar>().configureEach { dependsOn("generatePluginVersionSource") }

tasks.register("checkReadme").configure {
    inputs.file(versionDocFile)
    dependsOn("generatePluginVersionSource")
    doLast {
        val gitStatus = providers.exec {
            commandLine("git", "status")
        }.standardOutput.asText.get()
        if (gitStatus.contains("docs/dependencies.md")) throw GradleException("dependencies.md not up-to-date, please run ./gradlew generatePluginVersionSource (automatically run on most build jobs)")
    }
}

fun convertVersionLineToKotlinVersionLine(versionLine: String): String = buildString {
    append("internal const val ")
    val splitVersionLine = versionLine.trim().split(" ")
    append(splitVersionLine[0].trim().uppercase() + "_VERSION ")
    append(splitVersionLine[1].trim() + " ")
    append(splitVersionLine[2].trim())
}

fun updateReadmeVersionLine(readmeVersionLine: String, versionLines: List<String>): String {
    for (versionLine in versionLines) {
        if (readmeVersionLine.contains(versionLine.split("=").first().trim(), ignoreCase = true)) {
            return readmeVersionLine.split("=").first()
                .trim() + " = " + versionLine.split("=")[1].trim().trim("\"".first())
        }
    }
    return readmeVersionLine
}

fun extractVersionLines(file: File): List<String> {
    val fileLines = file.readLines()
    var isInVersionsSection = false
    val listOfVersionLines = mutableListOf<String>()
    for (line in fileLines) {
        if (line.startsWith("[")) {
            isInVersionsSection = line.contains("[versions]")
        }
        if (isInVersionsSection && line.contains("=")) {
            listOfVersionLines.add(line.split("#").first()) // remove comments
        }
    }
    return listOfVersionLines
}
