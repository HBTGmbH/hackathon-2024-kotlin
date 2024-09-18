plugins {
    id("hbt.spring-boot-cloud-application")
    id("hbt.kotlin")
    id("hbt.openapi")
    kotlin("kapt")
}

dependencies {
    implementation(project(":support"))

    implementation(libs.mapstruct.base)
    annotationProcessor(libs.mapstruct.processor)
    kapt(libs.mapstruct.processor)

    implementation(libs.bundles.spring.boot.security)
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.+")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.17.+")
}
