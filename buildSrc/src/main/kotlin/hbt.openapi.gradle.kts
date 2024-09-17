import org.jetbrains.kotlin.gradle.internal.KaptGenerateStubsTask

plugins {
    id("org.openapi.generator")
    kotlin("jvm")
}

openApiGenerate {
    generatorName.set("kotlin")
    inputSpec.set("$rootDir/module-server/src/main/resources/gtiApiDoc.yaml")
    outputDir.set("${layout.buildDirectory.get()}/generated")
    apiPackage.set("de.hbt.geofox.gti.api")
    invokerPackage.set("de.hbt.geofox.gti.invoker")
    modelPackage.set("de.hbt.geofox.gti.model")
    configOptions.set(mapOf("serializationLibrary" to "jackson", "library" to "jvm-ktor"))
    globalProperties.set(mapOf("models" to ""))
    generateModelDocumentation.set(false)
}

sourceSets {
    main {
        kotlin.srcDir("${layout.buildDirectory.get()}/generated/src/main/kotlin")
    }
}

openApiValidate {
    inputSpec.set("$rootDir/module-server/src/main/resources/gtiApiDoc.yaml")
}

tasks.named("compileKotlin") {
    dependsOn("openApiGenerate")
}

tasks.withType<KaptGenerateStubsTask>().configureEach {
    dependsOn("openApiGenerate")
}