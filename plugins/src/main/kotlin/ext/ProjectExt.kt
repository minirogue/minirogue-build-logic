package ext

import org.gradle.api.Project
import java.time.Instant
import java.time.ZoneOffset

@Suppress("MagicNumber")
internal fun getDateAsVersionName(): String {
    val now = Instant.now().atOffset(ZoneOffset.UTC)
    return "${now.year % 100}.${now.monthValue}.${now.dayOfMonth}"
}

private val Project.modulePath
    get() = path.split(":").drop(1).filter { it != "public" }

internal fun Project.generateProjectNamespace(): String = "com." + rootProject.name +
    modulePath.joinToString(separator = ".", prefix = ".")
        .replace("-", ".")
        .trimEnd { it == ".".first() }

internal fun Project.generateResourcePrefix(): String = modulePath.first { it != "feature" && it != "library" }
    .replace("-", "_") + "_"

internal fun Project.isMultiplatform(): Boolean = plugins.hasPlugin(
    "org.jetbrains.kotlin.multiplatform",
)
