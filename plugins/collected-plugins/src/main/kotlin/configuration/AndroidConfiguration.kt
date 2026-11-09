package configuration

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import com.android.build.gradle.api.KotlinMultiplatformAndroidPlugin
import ext.addToMinirogueCheck
import ext.generateProjectNamespace
import ext.getDateAsVersionName
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import task.CI_CHECK_TASK
import versions.JAVA_VERSION

private const val MIN_SDK = 24
private const val COMPILE_SDK = 37
private const val TARGET_SDK = 37

internal fun Project.configureAndroidMultiplatformLibrary() {
    with(pluginManager) { apply(KotlinMultiplatformAndroidPlugin::class.java) }
    extensions.getByType(KotlinMultiplatformExtension::class.java)
        .extensions.configure(
            KotlinMultiplatformAndroidLibraryTarget::class.java,
        ) {
            namespace = generateProjectNamespace()
            compileSdk = COMPILE_SDK
            minSdk = MIN_SDK

            androidResources { enable = true }

            withJava() // TODO need to understand this more.
            compilerOptions {
                jvmTarget.set(JvmTarget.fromTarget(JAVA_VERSION))
            }
            lint {
                baseline = file("lint-baseline.xml")
            }

            withHostTest {}
            withDeviceTest { instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner" }
        }
}

internal fun Project.configureAndroidApp() {
    extensions.configure(ApplicationExtension::class.java) {
        defaultConfig {
            compileSdk = COMPILE_SDK
            minSdk = MIN_SDK
            targetSdk = TARGET_SDK
            versionCode = findProperty("minirogue.build.number") as Int? ?: 1
            versionName = getDateAsVersionName()
            namespace = generateProjectNamespace()
            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
        buildTypes {
            release {
                isDebuggable = false
                isMinifyEnabled = true
                proguardFiles.add(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                )
                proguardFiles.add(project.file("proguard-rules.pro"))
            }
            debug {
                applicationIdSuffix = ".debug"
            }
        }
        compileOptions {
            sourceCompatibility = JavaVersion.toVersion(JAVA_VERSION)
            targetCompatibility = JavaVersion.toVersion(JAVA_VERSION)
        }
        lint {
            baseline = file("lint-baseline.xml")
        }
        tasks.named("lint").addToMinirogueCheck()
    }
    tasks.named(CI_CHECK_TASK) {
        dependsOn("testDebugUnitTest")
        dependsOn("assembleDebug")
    }
}
