package fr.fabien

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ApplicationAspirateur

fun main(args: Array<String>) {
    runApplication<ApplicationAspirateur>(*args)
}