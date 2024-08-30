plugins {
    id("hbt.spring-boot-cloud-base")
    id("hbt.kotlin")
    kotlin("kapt")
}

dependencies {
    implementation(libs.spring.boot.actuator)
    implementation(libs.spring.boot.web)
    implementation(libs.spring.openapi)

    implementation(libs.bundles.spring.boot.security)
}