plugins {
    java
    `java-library`
    id("hbt.base")
    application
}

val projectSourceCompatibility: String = rootProject.properties["projectSourceCompatibility"].toString()

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(projectSourceCompatibility))
    }
}

repositories {
    mavenCentral()
}

tasks.register("buildAll") {
    group = "build"
    dependsOn("build")
    dependsOn("test")
}

tasks.clean {
    doFirst {
        delete("out")
    }
}
