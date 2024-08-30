import org.gradle.accessors.dm.LibrariesForLibs
import org.springframework.boot.gradle.tasks.bundling.BootJar
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatter.ofPattern

plugins {
    id("hbt.spring-boot-base")
}

val libs = the<LibrariesForLibs>()

dependencies {
    implementation(libs.bundles.spring.boot)
    testImplementation(libs.spring.boot.test)
}

sourceSets {
    create("integration-test") {
        java {
            compileClasspath += sourceSets.main.get().output + sourceSets.test.get().output
            runtimeClasspath += sourceSets.main.get().output + sourceSets.test.get().output
            setSrcDirs(listOf("src/integration-test"))
        }
    }
}

idea {
    module {
        testSources.from(sourceSets["integration-test"].java.srcDirs)
    }
}

val integrationTestImplementation: Configuration by configurations.getting {
    extendsFrom(configurations.testImplementation.get())
}

tasks.register<Test>("integrationTest") {
    systemProperty("junit.jupiter.execution.parallel.enabled", true)
    systemProperty("junit.jupiter.execution.parallel.mode.default", "concurrent")
    systemProperty("junit.jupiter.execution.parallel.mode.classes.default", "concurrent")
    useJUnitPlatform()
    maxHeapSize = "4g"
    group = "verification"
    workingDir = rootProject.projectDir
    testClassesDirs = sourceSets["integration-test"].output.classesDirs
    classpath = sourceSets["integration-test"].runtimeClasspath
}

tasks.named("buildAll") {
    dependsOn("integrationTest")
}

val formatter: DateTimeFormatter = ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")

tasks.jar {
    manifest {
        attributes["Implementation-Title"] = rootProject.name
        attributes["Implementation-Version"] = archiveVersion.get()
        attributes["Implementation-Vendor"] = "Jim Martens"
        attributes["Build-Timestamp"] = ZonedDateTime.now().format(formatter)
        attributes["Created-By"] = "Gradle ${gradle.gradleVersion}"
        attributes["Build-Jdk"] = "${providers.systemProperty("java.version").get()} (${providers.systemProperty("java.vendor").get()} ${providers.systemProperty("java.vm.version").get()})"
        attributes["Build-OS"] = "${providers.systemProperty("os.name").get()} ${providers.systemProperty("os.arch").get()} ${providers.systemProperty("os.version").get()}"
    }
}

springBoot {
    buildInfo()
    mainClass.set(project.properties["mainClass"].toString())
}

tasks.named<BootJar>("bootJar") {
    enabled = true
}