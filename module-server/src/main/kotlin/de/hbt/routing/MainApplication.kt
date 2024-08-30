package de.hbt.routing

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@SpringBootApplication(scanBasePackages = ["de.hbt.support", "de.hbt.routing"])
open class MainApplication

fun main(args: Array<String>) {
    runApplication<MainApplication>(*args)
}

