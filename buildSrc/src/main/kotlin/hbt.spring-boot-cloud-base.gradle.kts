import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    id("hbt.spring-boot-base")
}

val libs = the<LibrariesForLibs>()

dependencies {
    implementation(platform(libs.spring.cloud))
}