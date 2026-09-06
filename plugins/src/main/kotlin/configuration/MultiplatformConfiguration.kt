package configuration

import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import task.CI_CHECK_TASK
import task.SourceType

internal fun Project.configureKotlinMultiplatformAndroid() {
    configureAndroidMultiplatformLibrary()
    configureCreateSrc(SourceType.AndroidMultiplatform)
    configureTest(SourceType.AndroidMultiplatform)
    tasks.named(CI_CHECK_TASK) { dependsOn("testAndroidHostTest") }
}

internal fun Project.configureKotlinMultiplatformJvm() {
    configureJvm()
    extensions.configure(KotlinMultiplatformExtension::class.java) {
        jvm()
    }
    configureCreateSrc(SourceType.JvmMultiplatform)
    configureTest(SourceType.JvmMultiplatform)
    tasks.named(CI_CHECK_TASK) { dependsOn("jvmTest") }
}

internal fun Project.configureKotlinMultiplatformIOS() {
    extensions.configure(KotlinMultiplatformExtension::class.java) {
        listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach {
            it.binaries.framework {
                baseName = (project.parent?.name ?: "") + project.name
                isStatic = true
            }
        }
    }
    configureCreateSrc(SourceType.IosMultiplatform)
//    configureTest(SourceType.IosMultiplatform) TODO
//    tasks.named(CI_CHECK_TASK) { dependsOn("iosTest") } // TODO make sure this doesn't break non-mac environments
}
