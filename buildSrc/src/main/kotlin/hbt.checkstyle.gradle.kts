plugins {
    checkstyle
    id("hbt.java-base")
}

checkstyle {
    toolVersion = "10.0"
    isIgnoreFailures = false
    maxWarnings = 0

    configFile = rootProject.file("config/checkstyle/checkstyle.xml")

    configProperties = mapOf<String, String>(
            "org.checkstyle.google.suppressionfilter.config" to
            "${project.rootDir}/config/checkstyle/checkstyle-suppressions.xml")

}
tasks.withType<Checkstyle>().configureEach {
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}
