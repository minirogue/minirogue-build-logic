package ext

internal enum class Platform(val mainSource: String, val kspConfigurationName: String) {
    JVM(mainSource = "jvmMain", "kspJvm"),
    Android(mainSource = "androidMain", "kspAndroid"),
    IOSArm64(mainSource = "iosArm64Main", "kspIosArm64"),
    IOSX64(mainSource = "iosX64Main", "kspIosX64"),
    IOSSimulator(mainSource = "iosSimulatorArm64Main", "kspIosSimulatorArm64"),
}
