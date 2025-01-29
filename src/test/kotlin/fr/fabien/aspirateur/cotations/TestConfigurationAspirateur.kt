package fr.fabien.aspirateur.cotations

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.batch.core.*
import org.springframework.batch.test.JobLauncherTestUtils
import org.springframework.batch.test.JobRepositoryTestUtils
import org.springframework.batch.test.context.SpringBatchTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import kotlin.test.AfterTest

@ActiveProfiles("test")
@SpringBatchTest
@SpringBootTest
//@SpringBootTest(classes = [ConfigurationAspirateur::class])
class TestConfigurationAspirateur {

    @Autowired
    private val jobLauncherTestUtils: JobLauncherTestUtils? = null

    @Autowired
    private val jobRepositoryTestUtils: JobRepositoryTestUtils? = null

    @Autowired
    private val jobMajLibelles: Job? = null

    @AfterTest
    fun tearDown() {
        jobRepositoryTestUtils?.removeJobExecutions()
    }

    @Test
    @Throws(Exception::class)
    fun launchJob_WhenJobEnds_ThenStatusCompleted() {
        jobLauncherTestUtils?.setJob(jobMajLibelles!!) // job is launch here ?!?
        val jobExecution: JobExecution = jobLauncherTestUtils?.launchJob()!!
        // TODO : check table not empty !
        // TODO : check starting spring boot log
        // TODO : it works with a wrong configuration ? ConfigurationDataSourceBusiness & ConfigurationDataSourceJobRepository
        Assertions.assertEquals(ExitStatus.COMPLETED, jobExecution.exitStatus)
    }
}