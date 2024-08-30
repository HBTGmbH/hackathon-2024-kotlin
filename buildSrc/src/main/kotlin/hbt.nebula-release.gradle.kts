import java.nio.file.Files

plugins {
    id("hbt.base")
}

apply(plugin="com.netflix.nebula.release")

tasks.register("writeVersionProperties") {
    group = "version"
    mustRunAfter("release")
    outputs.file("${layout.buildDirectory.get().asFile}/version.properties")
    val directory = layout.buildDirectory.get().asFile
    doLast {
        Files.createDirectories(directory.toPath())
        File("${layout.buildDirectory.get().asFile}/version.properties").writeText("VERSION=${project.version}\n")
    }
}