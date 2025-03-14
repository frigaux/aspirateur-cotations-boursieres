package fr.fabien.aspirateur.cotations

import mu.KotlinLogging
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.context.ApplicationContext
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.system.exitProcess

@SpringBootApplication
@EntityScan("fr.fabien")
@EnableJpaRepositories("fr.fabien")
class ApplicationAspirateur(val jobLauncher: JobLauncher, val context: ApplicationContext) : CommandLineRunner {
    companion object {
        // job parameters keys
        val DATE: String = "date"
        private val logger = KotlinLogging.logger {}
    }

    override fun run(vararg args: String) {
        val jobName: String? = System.getProperty("JOB_NAME")
        val strDate: String? = System.getProperty("DATE")
        if (jobName != null && strDate != null) {
            logger.info { "Date = $strDate" }
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
 * On peut spécifier le profil avec la propriété système "-Dspring.profiles.active=dev".
 * @param args command line arguments pour SpringApplicationBuilder
 */
fun main(vararg args: String) {
    if (System.getProperty("JOB_NAME") == null || System.getProperty("DATE") == null) {
        KotlinLogging.logger {}.error { "Propriétés systèmes manquantes : JOB_NAME et DATE (dd/MM/yyyy)" }
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