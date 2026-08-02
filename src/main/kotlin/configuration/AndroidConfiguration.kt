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
import org.jetbrains.kotlin.konan.file.File
import task.CreateVersionCodeFileTask
import task.GetGitCommitNumberTask
import task.MINIROGUE_TASK_GROUP
import versions.JAVA_VERSION
import java.io.IOException

private const val MIN_SDK = 23
private const val COMPILE_SDK = 36
private const val TARGET_SDK = 36

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
            tasks.named("lint").addToMinirogueCheck()

            withHostTest {}
            withDeviceTest { instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner" }
        }
}

internal fun Project.configureAndroidApp() {
    extensions.configure(ApplicationExtension::class.java) {
        configureCreateAndroidVersionCodeTask() // TODO add end-user configurability
        defaultConfig {
            compileSdk = COMPILE_SDK
            minSdk = MIN_SDK
            targetSdk = TARGET_SDK
            versionCode = getVersionCodeFromPropertyFile()
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
}
private fun Project.configureCreateAndroidVersionCodeTask() {
    tasks.register("getGitCommitNumber", GetGitCommitNumberTask::class.java) {
        group = MINIROGUE_TASK_GROUP
        description = "Gets number of git commits in current branch's history."
    }
    // TODO add end-user configuration for source of version code
    tasks.register(
        "createAndroidVersionCode", // TODO extract to constant name
        CreateVersionCodeFileTask::class.java,
    ) {
        dependsOn("getGitCommitNumber")
        versionCodeSource.set(
            tasks.named(
                "getGitCommitNumber",
                GetGitCommitNumberTask::class.java,
            )
                .get().numberOfCommitsInBranchHistory,
        )

        group = MINIROGUE_TASK_GROUP
        description = "Creates versionCode file that is used for Android app"
    }
}

// TODO would like this to be verified as up-to-date at configuration time, but that would violate
//  guidelines about configuration
private fun Project.getVersionCodeFromPropertyFile(): Int = try {
    val versionFile =
        layout.buildDirectory.file("minirogue${File.separator}versionCode") // TODO extract to configurable location
    versionFile.get().asFile.readText().trim().toIntOrNull()
        ?: 1.also {
            logger.warn(
                "versionCode not properly formatted in $versionFile, defaulting to 1",
            )
        }
} catch (ioException: IOException) {
    logger.warn(
        "${ioException.message} \n Couldn't read versionCode from createAndroidVersionCode task." +
            " Ensure it has been run at least once since the last clean build. " +
            "Defaulting to version code 1.",
    )
    1
}
