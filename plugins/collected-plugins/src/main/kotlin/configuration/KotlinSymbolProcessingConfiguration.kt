package configuration

import com.google.devtools.ksp.gradle.KspGradleSubplugin
import org.gradle.api.plugins.PluginManager

internal fun PluginManager.applyKsp() {
    apply(KspGradleSubplugin::class.java)
}
