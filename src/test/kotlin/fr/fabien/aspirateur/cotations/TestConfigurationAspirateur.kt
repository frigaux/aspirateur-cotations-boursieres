package fr.fabien.aspirateur.cotations

import fr.fabien.aspirateur.cotations.ApplicationAspirateur.Companion.DATE
import fr.fabien.aspirateur.cotations.repository.RepositoryAbcCotation
import fr.fabien.aspirateur.cotations.repository.RepositoryAbcLibelle
import org.junit.jupiter.api.*
import org.springframework.batch.core.*
import org.springframework.batch.test.JobLauncherTestUtils
import org.springframework.batch.test.context.SpringBatchTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.time.DayOfWeek
import java.time.LocalDate


/**
 * Les tests ne sont pas mockés.
 * Ce sont des tests d'intégration de bout en bout.
 * Seules les DataSources sont mockées avec un HSQLDB monté en mémoire pour la durée des tests (voir @Profile("!test"))
 */
@ActiveProfiles("test")
@SpringBatchTest
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class TestConfigurationAspirateur(
    @Autowired private val jobLauncherTestUtils: JobLauncherTestUtils,
    @Autowired private val jobMajAbcLibelles: Job,
    @Autowired private val jobMajAbcCotations: Job,
    @Autowired private val repositoryAbcLibelle: RepositoryAbcLibelle,
    @Autowired private val repositoryAbcCotation: RepositoryAbcCotation
) {

    companion object {
        val jobParameters: JobParameters = JobParametersBuilder().addLocalDate(DATE, lastOpenDay()).toJobParameters()

        private fun lastOpenDay(): LocalDate {
            return LocalDate.now()
                .minusDays(1)
                .let {
                    var date: LocalDate = it
                    while (date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY) {
                        date = date.minusDays(1)
                    }
                    date
                }
        }
    }

    @Test
    @Order(1)
    @Throws(Exception::class)
    fun launchJobMajLibelles_WhenJobEnds_ThenThereAreLibelleInRepository() {
        jobLauncherTestUtils.setJob(jobMajAbcLibelles)
        val jobExecution: JobExecution = jobLauncherTestUtils.launchJob(jobParameters)
        Assertions.assertEquals(ExitStatus.COMPLETED, jobExecution.exitStatus)
        Assertions.assertTrue(repositoryAbcLibelle.count() > 0)
    }

    @Test
    @Order(2)
    @Throws(Exception::class)
    fun launchJobMajCotations_WhenJobEnds_ThenThereAreCotationInRepository() {
        jobLauncherTestUtils.setJob(jobMajAbcCotations)
        val jobExecution: JobExecution = jobLauncherTestUtils.launchJob(jobParameters)
        Assertions.assertEquals(ExitStatus.COMPLETED, jobExecution.exitStatus)
        Assertions.assertTrue(repositoryAbcCotation.count() > 0)
    }
}