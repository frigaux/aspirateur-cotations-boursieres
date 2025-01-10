package fr.fabien

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.batch.core.*
import org.springframework.batch.test.JobLauncherTestUtils
import org.springframework.batch.test.JobRepositoryTestUtils
import org.springframework.batch.test.context.SpringBatchTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.AfterTest

@SpringBatchTest
@SpringBootTest
class TestConfigurationAspirateur {

    @Autowired
    private val jobLauncherTestUtils: JobLauncherTestUtils? = null

    @Autowired
    private val jobRepositoryTestUtils: JobRepositoryTestUtils? = null

    @Autowired
    private val jobTasklet: Job? = null

    @AfterTest
    fun tearDown() {
        jobRepositoryTestUtils?.removeJobExecutions()
    }

    @Test
    @Throws(Exception::class)
    fun launchJob_WhenJobEnds_ThenStatusCompleted() {
        jobLauncherTestUtils?.setJob(jobTasklet!!) // job is launch here ?!?
        val jobExecution: JobExecution = jobLauncherTestUtils?.launchJob()!!
        Assertions.assertEquals(ExitStatus.COMPLETED, jobExecution.exitStatus)
    }
}