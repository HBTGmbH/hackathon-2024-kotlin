import org.gradle.accessors.dm.LibrariesForLibs
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("org.springframework.boot")
    id("hbt.java")
}

val libs = the<LibrariesForLibs>()

dependencies {
    implementation(platform(libs.spring.boot))
    implementation(libs.spring.boot.log4j)
}

configurations {
    configureEach {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
}

tasks.named<BootJar>("bootJar") {
    enabled = false
}