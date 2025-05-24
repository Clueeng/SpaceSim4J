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
}
tasks.test {
    useJUnitPlatform()
}