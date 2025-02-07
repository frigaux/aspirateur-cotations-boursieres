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
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.system.exitProcess

// TODO : JOB moyenne mobile
@SpringBootApplication
class ApplicationAspirateur(val jobLauncher: JobLauncher, val context: ApplicationContext) : CommandLineRunner {
    companion object {
        // job parameters keys
        val DATE: String = "date"
    }

    override fun run(vararg args: String) {
        val jobName: String? = System.getenv("JOB_NAME")
        val strDate: String? = System.getenv("DATE")
        if (jobName != null && strDate != null) {
            val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            val job: Job = context.getBean(jobName) as Job
            val jobParameters = JobParametersBuilder()
                .addLocalDate(DATE, LocalDate.parse(strDate, formatter))
                .toJobParameters()
            jobLauncher.run(job, jobParameters)
        }
    }
}

/**
 * "mvn spring-boot:run" appelle la méthode "main".
 * "com.intellij.rt.junit.JUnitStarter" ou "mvn test" n'appellent pas La méthode "main".
 * https://www.baeldung.com/spring-profiles
 * On peut spécifier le profil avec la JVM System Parameter "-Dspring.profiles.active=dev".
 * @param args command line arguments pour SpringApplicationBuilder
 */
fun main(vararg args: String) {
    if (System.getenv("JOB_NAME") == null || System.getenv("DATE") == null) {
        KotlinLogging.logger {}.error { "Variables d'environnement manquantes : JOB_NAME et DATE (dd/MM/yyyy)" }
        exitProcess(1)
    } else {
        exitProcess(
            SpringApplication.exit(
                SpringApplicationBuilder()
                    .sources(ApplicationAspirateur::class.java)
                    .properties("spring.config.name:configuration")
                    .run(*args)
            )
        )
    }
}