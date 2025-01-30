package fr.fabien.aspirateur.cotations

import mu.KotlinLogging
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.context.ApplicationContext
import java.time.LocalDateTime
import kotlin.system.exitProcess

@SpringBootApplication
class ApplicationAspirateur(val jobLauncher: JobLauncher, val context: ApplicationContext) : CommandLineRunner {
    override fun run(vararg args: String) {
        System.getenv("JOB_NAME")?.let { jobName ->
            val job: Job = context.getBean(jobName) as Job
            val jobParameters = JobParametersBuilder()
                .addLocalDateTime("now", LocalDateTime.now())
                .toJobParameters()
            val jobLauncher: JobLauncher = context.getBean("jobLauncher") as JobLauncher
            jobLauncher.run(job, jobParameters);
        } ?: run {
            KotlinLogging.logger {}.error { "Missing environment variable : JOB_NAME" }
        }
    }
}

// -Dspring.config.name=configuration -Dspring.profiles.active=dev
fun main(vararg args: String) {
    exitProcess(
        SpringApplication.exit(
            SpringApplicationBuilder()
                .sources(ApplicationAspirateur::class.java)
                .properties("spring.config.name:configuration")
                .run(*args)
        )
    )
}