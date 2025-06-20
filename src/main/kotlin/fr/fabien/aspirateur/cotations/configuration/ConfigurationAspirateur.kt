package fr.fabien.aspirateur.cotations.configuration

import fr.fabien.aspirateur.cotations.dto.abcbourse.DtoAbcCotation
import fr.fabien.aspirateur.cotations.dto.abcbourse.DtoAbcLibelle
import fr.fabien.aspirateur.cotations.dto.boursorama.DtoBoursoramaCours
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

    // récupération des libellés sur ABCbourse et persistence dans le modèle ABC
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

    // récupération des cotations sur ABCbourse et persistence dans le modèle ABC
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

    // récupération des cours sur Boursorama et persistence dans le modèle Boursorama
    @Bean
    fun stepRecupererBoursoramaCours(
        jobRepository: JobRepository,
        transactionManager: JpaTransactionManager,
        taskletRecupererBoursoramaCours: Tasklet
    ): Step {
        return StepBuilder("stepRecupererBoursoramaCours", jobRepository)
            .tasklet(taskletRecupererBoursoramaCours, transactionManager)
            .build()
    }

    @Bean
    fun stepPersisterBoursoramaCours(
        jobRepository: JobRepository,
        transactionManager: JpaTransactionManager,
        readerBoursoramaCours: ItemReader<DtoBoursoramaCours>,
        writerBoursoramaCours: ItemWriter<DtoBoursoramaCours>
    ): Step {
        return StepBuilder("stepPersisterBoursoramaCours", jobRepository)
            .chunk<DtoBoursoramaCours, DtoBoursoramaCours>(10, transactionManager)
            .reader(readerBoursoramaCours)
            .writer(writerBoursoramaCours)
            .build()
    }

    @Bean
    fun jobMajBoursoramaCours(
        jobRepository: JobRepository,
        stepRecupererBoursoramaCours: Step,
        stepPersisterBoursoramaCours: Step
    ): Job {
        return JobBuilder("jobMajBoursoramaCours", jobRepository)
            .start(stepRecupererBoursoramaCours)
            .next(stepPersisterBoursoramaCours)
            .build()
    }

    // conversion modèle ABC ou boursorama -> modèle normalisé
    @Bean
    fun stepConvertirEnValeurCours(
        jobRepository: JobRepository,
        transactionManager: JpaTransactionManager,
        taskletConvertirEnValeurCours: Tasklet
    ): Step {
        return StepBuilder("stepConvertirEnValeurCours", jobRepository)
            .tasklet(taskletConvertirEnValeurCours, transactionManager)
            .build()
    }

    @Bean
    fun jobConvertirEnValeurCours(
        jobRepository: JobRepository,
        stepConvertirEnValeurCours: Step
    ): Job {
        return JobBuilder("jobConvertirEnValeurCours", jobRepository)
            .start(stepConvertirEnValeurCours)
            .build()
    }

    // calcul des moyennes mobiles
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
