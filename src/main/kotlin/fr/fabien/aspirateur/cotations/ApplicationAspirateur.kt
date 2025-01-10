package fr.fabien.aspirateur.cotations

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder

@SpringBootApplication
class ApplicationAspirateur

// -Dspring.config.name=configuration -Dspring.profiles.active=dev
fun main(args: Array<String>) {
    SpringApplicationBuilder()
        .sources(ApplicationAspirateur::class.java)
        .properties("spring.config.name:configuration")
        .run(*args)
}