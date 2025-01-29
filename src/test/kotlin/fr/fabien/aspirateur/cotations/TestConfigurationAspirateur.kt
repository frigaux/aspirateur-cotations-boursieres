package fr.fabien.aspirateur.cotations

import fr.fabien.aspirateur.cotations.repository.LibelleRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.batch.core.*
import org.springframework.batch.test.JobLauncherTestUtils
import org.springframework.batch.test.context.SpringBatchTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@SpringBatchTest
@SpringBootTest
class TestConfigurationAspirateur {

    @Autowired
    private val jobLauncherTestUtils: JobLauncherTestUtils? = null

    @Autowired
    private val jobMajLibelles: Job? = null

    @Autowired
    private val libelleRepository: LibelleRepository? = null

    @Test
    @Throws(Exception::class)
    fun launchJob_WhenJobEnds_ThenStatusCompleted() {
        jobLauncherTestUtils!!.setJob(jobMajLibelles!!) // job is launch here ?!?
        val jobExecution: JobExecution = jobLauncherTestUtils.launchJob()
        Assertions.assertEquals(ExitStatus.COMPLETED, jobExecution.exitStatus)
        Assertions.assertTrue(libelleRepository!!.count() > 0)
    }
}