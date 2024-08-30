plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
    implementation(libs.plugin.kotlin.gradle)
    implementation(libs.plugin.springboot)
    implementation(libs.plugin.lombok)
    implementation(libs.plugin.nebula.release)
    implementation(libs.plugin.gradle.versions)
    implementation(libs.plugin.version.catalog)
    implementation(libs.plugin.jib)
}
