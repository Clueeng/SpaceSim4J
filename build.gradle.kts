plugins {
    id("java")
    application
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "net.flaily"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val lwjglVersion = "3.3.3"
val imguiVersion = "1.86.11"
val lwjglModules = listOf("lwjgl", "lwjgl-glfw", "lwjgl-opengl", "lwjgl-stb")
val nativeClassifiers = listOf("natives-windows", "natives-linux", "natives-macos")

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    // LWJGL Modules + Natives for every OS
    lwjglModules.forEach { module ->
        implementation("org.lwjgl:$module:$lwjglVersion")

        nativeClassifiers.forEach { classifier ->
            runtimeOnly("org.lwjgl:$module:$lwjglVersion:$classifier")
        }
    }

    // JOML - Math library for 3D
    implementation("org.joml:joml:1.10.5")

    // JSON for config
    implementation("com.google.code.gson:gson:2.13.1")

// https://mvnrepository.com/artifact/org.apache.logging.log4j/log4j-core
    implementation("org.apache.logging.log4j:log4j-core:2.24.3")
    implementation("org.apache.logging.log4j:log4j-api:2.24.3")

    // ImGUI
    implementation("io.github.spair:imgui-java-binding:$imguiVersion")
    implementation("io.github.spair:imgui-java-lwjgl3:$imguiVersion")
    runtimeOnly("io.github.spair:imgui-java-lwjgl3:$imguiVersion")
    // implementation("io.github.spair:imgui-java-natives-windows:$imguiVersion")

    listOf("windows", "linux", "macos").forEach { os ->
        runtimeOnly("io.github.spair:imgui-java-natives-$os:$imguiVersion")
    }
}
tasks.test {
    useJUnitPlatform()
}

application{
    mainClass.set("net.flaily.Main")
}