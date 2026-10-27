plugins {
    `kotlin-dsl`
    `maven-publish`
    alias(libs.plugins.gradle.publish)
    alias(libs.plugins.detekt)
}

group = "org.minirogue"
version = findProperty("minirogue.version") as String

kotlin {
    explicitApiWarning()
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-parameters")
    }
}

java {
    sourceCompatibility = JavaVersion.toVersion(libs.versions.java.get())
    targetCompatibility = JavaVersion.toVersion(libs.versions.java.get())
}

tasks.test {
    useJUnitPlatform()
}

detekt {
//    config.setFrom(files("src/main/resources/detekt-config.yml"))
    buildUponDefaultConfig = true
    autoCorrect = true
}

dependencies {
    implementation(libs.develocity.gradlePlugin)
    implementation(libs.foojayResolver.gradlePlugin)

    detektPlugins(libs.detekt.formatting)
}

gradlePlugin {
    plugins {
        register("settings") {
            id = "minirogue.settings"
            implementationClass = "io.github.minirogue.plugin.SettingsConventionPlugin"
        }
    }
}

tasks.register("ciChecks") {
    description = "Run all checks for CI"
    dependsOn("build")
    dependsOn("detekt")
    dependsOn("publishToMavenLocal")
}
