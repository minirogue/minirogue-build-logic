package configuration

import androidx.room.gradle.RoomExtension
import androidx.room.gradle.RoomGradlePlugin
import ext.kspAll
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import versions.ROOM_VERSION

internal fun Project.configureRoomMultiplatform() {
    with(pluginManager) {
        applyKsp()
        apply(RoomGradlePlugin::class.java)
    }

    extensions.configure(RoomExtension::class.java) {
        schemaDirectory("$projectDir/schemas")
    }
    kspAll("androidx.room:room-compiler:$ROOM_VERSION")
    kotlinExtension.sourceSets.named("commonMain").configure {
        this.dependencies {
            implementation(
                "androidx.room:room-runtime:$ROOM_VERSION",
            )
        }
    }
}
