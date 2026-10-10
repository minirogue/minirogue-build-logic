package plugin

import com.gradle.develocity.agent.gradle.DevelocityPlugin
import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import org.gradle.kotlin.dsl.develocity
import org.gradle.toolchains.foojay.FoojayToolchainsConventionPlugin

public class SettingsConventionPlugin : Plugin<Settings> {
    override fun apply(target: Settings): Unit = with(target) {
        with(plugins) {
            apply(FoojayToolchainsConventionPlugin::class.java)
            apply(DevelocityPlugin::class.java)
        }
        dependencyResolutionManagement {
            repositories {
                google()
                mavenCentral()
            }
        }

        develocity {
            buildScan {
                termsOfUseUrl.set("https://gradle.com/help/legal-terms-of-use")
                termsOfUseAgree.set("yes")
                publishing.onlyIf { true }
            }
        }

        enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
    }
}
