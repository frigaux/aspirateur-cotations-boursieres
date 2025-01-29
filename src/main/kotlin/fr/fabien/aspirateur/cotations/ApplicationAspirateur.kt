package fr.fabien.aspirateur.cotations

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.context.ConfigurableApplicationContext
import kotlin.system.exitProcess

// TODO : mvn spring-boot:run + CommandLineJobRunner
//@SpringBootApplication
//class ApplicationAspirateur(val jobLauncher: JobLauncher, val applicationContext: ApplicationContext) : CommandLineRunner {
//    override fun run(vararg args: String?) {
//        val jobMajLibelles: Job = applicationContext.getBean("jobMajLibelles") as Job
//        val jobParameters = JobParametersBuilder()
//            .addLocalDateTime("now", LocalDateTime.now())
//            .toJobParameters()
//        val jobExecution: JobExecution = jobLauncher.run(jobMajLibelles, jobParameters);
//    }
//}

@SpringBootApplication
class ApplicationAspirateur

// -Dspring.config.name=configuration -Dspring.profiles.active=dev
fun main(args: Array<String>) {
    val context : ConfigurableApplicationContext = SpringApplicationBuilder()
        .sources(ApplicationAspirateur::class.java)
        .properties("spring.config.name:configuration")
        .run(*args)
//    for (nomJob: String in args) {
//        val job: Job = context.getBean(nomJob) as Job
//        val jobParameters = JobParametersBuilder()
//            .addLocalDateTime("now", LocalDateTime.now())
//            .toJobParameters()
//        val jobLauncher: JobLauncher = context.getBean("jobLauncher") as JobLauncher
//        jobLauncher.run(job, jobParameters);
//    }
    exitProcess(
        SpringApplication.exit(
            context
        )
    )
}