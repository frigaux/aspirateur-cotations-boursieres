package fr.fabien.aspirateur.cotations.configuration

import fr.fabien.aspirateur.cotations.dto.abcbourse.DtoAbcCotation
import fr.fabien.aspirateur.cotations.dto.abcbourse.DtoAbcLibelle
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
    fun stepRecupererAbcLibelles(
        jobRepository: JobRepository,
        transactionManager: JpaTransactionManager,
        taskletRecupererAbcLibelles: Tasklet
    ): Step {
        return StepBuilder("stepRecupererAbcLibelles", jobRepository)
            .tasklet(taskletRecupererAbcLibelles, transactionManager)
            .build()
    }

    @Bean
    fun stepPersisterAbcLibelles(
        jobRepository: JobRepository,
        transactionManager: JpaTransactionManager,
        readerAbcLibelle: ItemReader<DtoAbcLibelle>,
        writerAbcLibelle: ItemWriter<DtoAbcLibelle>
    ): Step {
        return StepBuilder("stepPersisterAbcLibelles", jobRepository)
            .chunk<DtoAbcLibelle, DtoAbcLibelle>(10, transactionManager)
            .reader(readerAbcLibelle)
            .writer(writerAbcLibelle)
            .build()
    }

    @Bean
    fun jobMajAbcLibelles(
        jobRepository: JobRepository,
        stepRecupererAbcLibelles: Step,
        stepPersisterAbcLibelles: Step
    ): Job {
        return JobBuilder("jobMajAbcLibelles", jobRepository)
            .start(stepRecupererAbcLibelles)
            .next(stepPersisterAbcLibelles)
            .build()
    }

    @Bean
    fun stepRecupererAbcCotations(
        jobRepository: JobRepository,
        transactionManager: JpaTransactionManager,
        taskletRecupererAbcCotations: Tasklet
    ): Step {
        return StepBuilder("stepRecupererAbcCotations", jobRepository)
            .tasklet(taskletRecupererAbcCotations, transactionManager)
            .build()
    }

    @Bean
    fun stepPersisterAbcCotations(
        jobRepository: JobRepository,
        transactionManager: JpaTransactionManager,
        readerAbcCotation: ItemReader<DtoAbcCotation>,
        writerAbcCotation: ItemWriter<DtoAbcCotation>
    ): Step {
        return StepBuilder("stepPersisterAbcCotations", jobRepository)
            .chunk<DtoAbcCotation, DtoAbcCotation>(10, transactionManager)
            .reader(readerAbcCotation)
            .writer(writerAbcCotation)
            .build()
    }

    @Bean
    fun jobMajAbcCotations(
        jobRepository: JobRepository,
        stepRecupererAbcCotations: Step,
        stepPersisterAbcCotations: Step
    ): Job {
        return JobBuilder("jobMajAbcCotations", jobRepository)
            .start(stepRecupererAbcCotations)
            .next(stepPersisterAbcCotations)
            .build()
    }

    @Bean
    fun stepAbcToValeurCours(
        jobRepository: JobRepository,
        transactionManager: JpaTransactionManager,
        taskletAbcToValeurCours: Tasklet
    ): Step {
        return StepBuilder("stepAbcToValeurCours", jobRepository)
            .tasklet(taskletAbcToValeurCours, transactionManager)
            .build()
    }

    @Bean
    fun jobAbcToValeurCours(
        jobRepository: JobRepository,
        stepAbcToValeurCours: Step
    ): Job {
        return JobBuilder("jobAbcToValeurCours", jobRepository)
            .start(stepAbcToValeurCours)
            .build()
    }

    @Bean
    fun stepCalculerMoyennes(
        jobRepository: JobRepository,
        transactionManager: JpaTransactionManager,
        taskletCalculerMoyennes: Tasklet
    ): Step {
        return StepBuilder("stepCalculerMoyennes", jobRepository)
            .tasklet(taskletCalculerMoyennes, transactionManager)
            .build()
    }

    @Bean
    fun jobCalculerMoyennes(
        jobRepository: JobRepository,
        stepCalculerMoyennes: Step
    ): Job {
        return JobBuilder("jobCalculerMoyennes", jobRepository)
            .start(stepCalculerMoyennes)
            .build()
    }
}
