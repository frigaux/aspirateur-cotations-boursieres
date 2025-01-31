package fr.fabien.aspirateur.cotations.configuration

import fr.fabien.aspirateur.cotations.dto.DtoCotation
import fr.fabien.aspirateur.cotations.dto.DtoLibelle
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
import org.springframework.orm.jpa.JpaTransactionManager


@Configuration
class ConfigurationAspirateur {

    @Bean
    fun stepRecupererLibelles(
        jobRepository: JobRepository,
        transactionManager: JpaTransactionManager,
        taskletRecupererLibelles: Tasklet
    ): Step {
        return StepBuilder("stepRecupererLibelles", jobRepository)
            .tasklet(taskletRecupererLibelles, transactionManager)
            .build()
    }

    @Bean
    fun stepPersisterLibelles(
        jobRepository: JobRepository,
        transactionManager: JpaTransactionManager,
        readerLibelle: ItemReader<DtoLibelle>,
        writerLibelle: ItemWriter<DtoLibelle>
    ): Step {
        return StepBuilder("stepPersisterLibelles", jobRepository)
            .chunk<DtoLibelle, DtoLibelle>(10, transactionManager)
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

    @Bean
    fun stepRecupererCotations(
        jobRepository: JobRepository,
        transactionManager: JpaTransactionManager,
        taskletRecupererCotations: Tasklet
    ): Step {
        return StepBuilder("stepRecupererCotations", jobRepository)
            .tasklet(taskletRecupererCotations, transactionManager)
            .build()
    }

    @Bean
    fun stepPersisterCotations(
        jobRepository: JobRepository,
        transactionManager: JpaTransactionManager,
        readerCotation: ItemReader<DtoCotation>,
        writerCotation: ItemWriter<DtoCotation>
    ): Step {
        return StepBuilder("stepPersisterCotations", jobRepository)
            .chunk<DtoCotation, DtoCotation>(10, transactionManager)
            .reader(readerCotation)
            .writer(writerCotation)
            .build()
    }

    @Bean
    fun jobMajCotations(
        jobRepository: JobRepository,
        stepRecupererCotations: Step,
        stepPersisterCotations: Step
    ): Job {
        return JobBuilder("jobMajCotations", jobRepository)
            .start(stepRecupererCotations)
            .next(stepPersisterCotations)
            .build()
    }
}
