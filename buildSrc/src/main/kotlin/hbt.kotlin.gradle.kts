import org.gradle.accessors.dm.LibrariesForLibs
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm")
}

val libs = the<LibrariesForLibs>()

dependencies {
    implementation(libs.kotlin.logging)
    implementation(kotlin("reflect"))
}

val projectSourceCompatibility: String = rootProject.properties["projectSourceCompatibility"].toString()

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjvm-default=all", "-Xjsr305=strict")
        jvmTarget.set(JvmTarget.fromTarget(projectSourceCompatibility))
    }
}