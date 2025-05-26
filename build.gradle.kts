plugins {
    id("java")
}

group = "net.flaily"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val lwjglVersion = "3.3.3"
val lwjglNatives = "natives-windows" // Change to: natives-linux / natives-macos if needed
val imguiVersion = "1.86.11"

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    // LWJGL Core
    implementation("org.lwjgl:lwjgl:$lwjglVersion")
    runtimeOnly("org.lwjgl:lwjgl::$lwjglNatives")

    // GLFW (for window/input/context)
    implementation("org.lwjgl:lwjgl-glfw:$lwjglVersion")
    runtimeOnly("org.lwjgl:lwjgl-glfw::$lwjglNatives")

    // OpenGL bindings
    implementation("org.lwjgl:lwjgl-opengl:$lwjglVersion")
    runtimeOnly("org.lwjgl:lwjgl-opengl::$lwjglNatives")

    // stb (for image loading)
    implementation("org.lwjgl:lwjgl-stb:$lwjglVersion")
    runtimeOnly("org.lwjgl:lwjgl-stb::$lwjglNatives")

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
    implementation("io.github.spair:imgui-java-natives-windows:$imguiVersion")
}
tasks.test {
    useJUnitPlatform()
}