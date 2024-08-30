plugins {
    id("hbt.jib")
}

dependencies {
    implementation(project(":server"))
}

jib {
    from {
        image = "amazoncorretto:" + properties["projectSourceCompatibility"] + "-alpine"
        platforms {
            platform {
                architecture = "amd64"
                os = "linux"
            }
            platform {
                architecture = "arm64"
                os = "linux"
            }
        }
    }
    to {
        image = "hbt/routing"
        tags = setOf(
                "latest",
                properties["version"].toString().replace("+", "-"))
        auth {
            username = System.getenv("USERNAME")
            password = System.getenv("PASSWORD")
        }
    }
    container {
        mainClass = "de.hbt.routing.MainApplicationKt"
        jvmFlags = listOf("-XX:+UseContainerSupport",
                "-XX:MaxRAMPercentage=75.0")
    }
}
