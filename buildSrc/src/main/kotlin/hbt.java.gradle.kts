import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    jacoco
    id("io.freefair.lombok")
    id("hbt.java-base")
    id("hbt.checkstyle")
}

val libs = the<LibrariesForLibs>()

dependencies {
    constraints.implementation(libs.bundles.logging)

    implementation(libs.slf4j.api)
    runtimeOnly(libs.bundles.logging)

    testImplementation(libs.bundles.test)
    testRuntimeOnly(libs.junit.launcher)
}

configurations {
    configureEach {
        exclude(group="junit", module="junit")
        // we are using log4j-slf4j2-impl, so we need to suppress spring include of log4j-slf4j-impl
        exclude(group="org.apache.logging.log4j", module="log4j-slf4j-impl")
    }
}

tasks.withType<Test>().configureEach {
    systemProperty("junit.jupiter.execution.parallel.enabled", true)
    systemProperty("junit.jupiter.execution.parallel.mode.default", "concurrent")
    useJUnitPlatform()
    maxHeapSize = "4g"
    workingDir = rootProject.projectDir
    finalizedBy(tasks.jacocoTestReport)
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
    }
}

normalization.runtimeClasspath.metaInf {
    ignoreAttribute("Build-Timestamp")
}



tasks.register("cleanLibs") {
    delete("${layout.buildDirectory.get().asFile}/libs")
}

tasks.build {
    dependsOn("cleanLibs")
}
