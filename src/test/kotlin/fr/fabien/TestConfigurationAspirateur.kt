package fr.fabien

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.batch.core.*
import org.springframework.batch.test.JobLauncherTestUtils
import org.springframework.batch.test.JobRepositoryTestUtils
import org.springframework.batch.test.context.SpringBatchTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestExecutionListeners
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener
import org.springframework.test.context.support.DirtiesContextTestExecutionListener

@ActiveProfiles("test")
@TestExecutionListeners(
    DependencyInjectionTestExecutionListener::class, DirtiesContextTestExecutionListener::class
) // This is to avoid clashing of several JobRepository instances using the same data source for several test classes
// This is to avoid clashing of several JobRepository instances using the same data source for several test classes
// This is to avoid clashing of several JobRepository instances using the same data source for several test classes
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBatchTest
@SpringBootTest
class TestConfigurationAspirateur {

    @Autowired
    private val jobLauncherTestUtils: JobLauncherTestUtils? = null

    @Autowired
    private val jobRepositoryTestUtils: JobRepositoryTestUtils? = null

    @Autowired
    private val jobTasklet: Job? = null

    @BeforeEach
    fun setUp() {
        jobLauncherTestUtils?.setJob(jobTasklet!!)
    }

    @AfterEach
    fun tearDown() {
        jobRepositoryTestUtils?.removeJobExecutions()
    }

    @Test
    @Throws(Exception::class)
    fun importUserJob_WhenJobEnds_ThenStatusCompleted() {
        val jobExecution: JobExecution = jobLauncherTestUtils?.launchJob()!!
        Assertions.assertEquals(ExitStatus.COMPLETED, jobExecution.exitStatus)
    }


//    @Test
//    @Throws(Exception::class)
//    fun testLaunchJob() {
//        // given
//        val context: ApplicationContext = AnnotationConfigApplicationContext(ConfigurationAspirateur::class.java)
//        val jobLauncher = context.getBean(JobLauncher::class.java)
//        val job = context.getBean(Job::class.java)
//
//        // when
//        val jobExecution = jobLauncher.run(job, JobParameters())
//
//        // then
//        Assertions.assertEquals(BatchStatus.COMPLETED, jobExecution.status)
//    }
}