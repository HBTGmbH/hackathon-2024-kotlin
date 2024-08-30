plugins {
    id("com.google.cloud.tools.jib")
    id("hbt.java-base")
}

tasks.named("jib") {
    dependsOn("build")
}

tasks.named("jibDockerBuild") {
    dependsOn("build")
}

tasks.named("build") {
    dependsOn("cleanCache")
}

tasks.register("cleanCache") {
    delete("${layout.buildDirectory.get().asFile}/jib-cache")
    delete("${layout.buildDirectory.get().asFile}/libs")
}
