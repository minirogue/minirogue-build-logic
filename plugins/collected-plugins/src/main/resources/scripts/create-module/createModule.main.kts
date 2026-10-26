#!/usr/bin/env kotlin

package scripts.`create-module`

import java.io.File
import java.io.FileOutputStream
import java.util.Locale

createModules()

enum class ModuleType(val directoryName: String) {
    Feature("feature"),
    Library("library"),
}


fun createModules() { // Main function
    val moduleType = getGradleModuleType()
    println("got module type: $moduleType")
    val moduleName = getGradleModuleName()
    println("got module name: $moduleName")

    val rootDirectory = getRootDirectory()
    println("found root directory to be $rootDirectory")
    val scriptsFolder = getScriptsDirectory()
    println("found scripts directory: $scriptsFolder")
    val parentDirectory = getParentDirectory(rootDirectory, moduleType, moduleName)

    println("Creating internal module")
    val internalGradleFile = File("$parentDirectory${File.separator}internal${File.separator}build.gradle.kts")
    val internalGradleContent = File("$scriptsFolder${File.separator}internal-template.gradle.kts").readText()
        .replace(
            "PUBLIC_PLACEHOLDER",
            "projects.${moduleType.directoryName}.${moduleName.convertKebabToCamelCase()}.public"
        )
    internalGradleFile.apply {
        parentFile?.mkdirs()
        writeText(internalGradleContent)
    }

    println("Creating public module")
    val publicGradleFile = File("$parentDirectory${File.separator}public${File.separator}build.gradle.kts")
    File("$scriptsFolder${File.separator}public-template.gradle.kts").copyTo(publicGradleFile)

    println("Updating settings.gradle(.kts)")
    val settingsGradle = File("$rootDirectory${File.separator}settings.gradle")
    val settingsGradleKts = File("$rootDirectory${File.separator}settings.gradle.kts")
    val actualSettingsGradle = when {
        settingsGradleKts.exists() -> settingsGradleKts
        settingsGradle.exists() -> settingsGradle
        else -> error("Cannot find settings.gradle(.kts) in $rootDirectory")
    }
    FileOutputStream(actualSettingsGradle, true).bufferedWriter().use { writer ->
        writer.appendLine("include(\":${moduleType.directoryName}:$moduleName:internal\")")
        writer.appendLine("include(\":${moduleType.directoryName}:$moduleName:public\")")
    }

    println("Adding source directories")
    ProcessBuilder().directory(File(rootDirectory))
        .command("./gradlew", ":${moduleType.directoryName}:$moduleName:internal:createSrc")
        .inheritIO()
        .start()
        .waitFor()
    ProcessBuilder().directory(File(rootDirectory))
        .command("./gradlew", ":${moduleType.directoryName}:$moduleName:public:createSrc")
        .inheritIO()
        .start()
        .waitFor()
}

fun getRootDirectory(): String {
    var gradlewFile = File("gradlew")
    while (!gradlewFile.exists()) {
        gradlewFile = File("../" + gradlewFile.path)
    }
    return gradlewFile.absoluteFile.parent ?: error("gradlew parent not found")
}

fun getScriptsDirectory(): String {
    return __FILE__.absoluteFile.parent ?: error("scripts directory not found")
}

fun getGradleModuleType(): ModuleType {
    println("(f)eature or (l)ibrary?")
    var newModuleType: ModuleType? = null
    while (newModuleType == null) {
        val input = readln().trim().lowercase()
        newModuleType = when (input) {
            "f" -> ModuleType.Feature
            "l" -> ModuleType.Library
            else -> null.also { println("Please input just \"f\" or \"l\"") }
        }
    }
    return newModuleType
}

fun getGradleModuleName(): String {
    println("What do you want to name the module?")
    var newModuleName: String? = null
    val moduleNameRules = Regex("^[a-z-]+$") // kebabcase
    while (newModuleName == null) {
        val input = readln().trim().lowercase()
        if (moduleNameRules.matches(input)) {
            newModuleName = input
        } else {
            println("Module name not valid, use only lowercase characters and hyphens")
        }
    }
    return newModuleName
}

fun getParentDirectory(rootDirectory: String, moduleType: ModuleType, moduleName: String): String {
    return "$rootDirectory${File.separator}${moduleType.directoryName}${File.separator}$moduleName"
}

fun String.convertKebabToCamelCase(): String {
    val snakeRegex = "-[a-zA-Z]".toRegex()

    return snakeRegex.replace(this) {
        it.value.replace("-", "")
            .uppercase(Locale.getDefault())
    }
}
