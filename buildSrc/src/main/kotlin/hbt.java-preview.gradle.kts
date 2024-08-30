plugins {
    id("hbt.java-base")
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.plusAssign("--enable-preview")
}

tasks.withType<Test>().configureEach {
    jvmArgs.plusAssign("--enable-preview")
}

tasks.withType<JavaExec>().configureEach {
    jvmArgs.plusAssign("--enable-preview")
}