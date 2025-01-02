package fr.fabien.configuration

import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.StepContribution
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.support.JdbcTransactionManager

@Configuration
class ConfigurationAspirateur {
    @Bean
    fun stepTasklet(jobRepository: JobRepository, transactionManager: JdbcTransactionManager): Step {
        return StepBuilder(
            "stepTasklet",
            jobRepository
        ).tasklet({ contribution: StepContribution, chunkContext: ChunkContext ->
            println("Hello world!")
            RepeatStatus.FINISHED
        }, transactionManager).build()
    }

    @Bean
    fun jobTasklet(jobRepository: JobRepository, step: Step): Job {
        return JobBuilder("jobTasklet", jobRepository).start(step).build()
    }
}
