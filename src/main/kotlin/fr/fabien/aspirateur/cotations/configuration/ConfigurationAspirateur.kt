package fr.fabien.aspirateur.cotations.configuration

import fr.fabien.aspirateur.cotations.job.dto.Libelle
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.item.ItemReader
import org.springframework.batch.item.ItemWriter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.support.JdbcTransactionManager

@Configuration
class ConfigurationAspirateur {

    @Bean
    fun stepRecupererLibelles(
        jobRepository: JobRepository,
        transactionManager: JdbcTransactionManager,
        taskletRecupererLibelles: Tasklet
    ): Step {
        return StepBuilder("stepRecupererLibelles", jobRepository)
            .tasklet(taskletRecupererLibelles, transactionManager)
            .build()
    }

    @Bean
    fun stepPersisterLibelles(
        jobRepository: JobRepository,
        transactionManager: JdbcTransactionManager,
        readerLibelle: ItemReader<Libelle>,
        writerLibelle: ItemWriter<Libelle>
    ): Step {
        return StepBuilder("stepPersisterLibelles", jobRepository)
            .chunk<Libelle, Libelle>(10, transactionManager)
            .reader(readerLibelle)
            .writer(writerLibelle)
            .build()
    }

    @Bean
    fun jobMajLibelles(
        jobRepository: JobRepository,
        stepRecupererLibelles: Step,
        stepPersisterLibelles: Step
    ): Job {
        return JobBuilder("jobMajLibelles", jobRepository)
            .start(stepRecupererLibelles)
            .next(stepPersisterLibelles)
            .build()
    }
}
