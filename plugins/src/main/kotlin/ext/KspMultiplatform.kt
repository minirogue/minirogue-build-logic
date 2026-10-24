package ext

import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension

internal fun Project.kspAll(dependencyNotation: Any) {
    kotlinExtension.sourceSets.apply {
        for (platform in Platform.entries) {
            if (any { it.name == platform.mainSource }) {
                dependencies.add(
                    platform.kspConfigurationName,
                    dependencyNotation,
                )
            }
        }
    }
}
