import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    id("hbt.spring-boot-application")
    id("hbt.spring-boot-cloud-base")
}

val libs = the<LibrariesForLibs>()

dependencies {
    implementation(libs.bundles.spring.boot.server)
    implementation(libs.spring.openapi)

    implementation(libs.httpclient)
    implementation(libs.prometheus)
}

sourceSets {
    create("server-test") {
        java {
            compileClasspath += sourceSets.main.get().output + sourceSets.test.get().output
            runtimeClasspath += sourceSets.main.get().output + sourceSets.test.get().output
            setSrcDirs(listOf("src/server-test"))
        }
    }
}

idea {
    module {
        testSources.from(sourceSets["server-test"].java.srcDirs)
    }
}

val serverTestImplementation: Configuration by configurations.getting {
    extendsFrom(configurations["testImplementation"])
}

tasks.register<Test>("serverTest") {
    outputs.upToDateWhen { false }
    systemProperty("junit.jupiter.execution.parallel.enabled", true)
    systemProperty("junit.jupiter.execution.parallel.mode.default", "concurrent")
    systemProperty("junit.jupiter.execution.parallel.mode.classes.default", "concurrent")
    useJUnitPlatform()
    maxHeapSize = "4g"
    group = "verification"
    workingDir = rootProject.projectDir
    testClassesDirs = sourceSets["server-test"].output.classesDirs
    classpath = sourceSets["server-test"].runtimeClasspath
}
