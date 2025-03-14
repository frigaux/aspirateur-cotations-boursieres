package fr.fabien.aspirateur.cotations

import fr.fabien.aspirateur.cotations.ApplicationAspirateur.Companion.DATE
import fr.fabien.jpa.cotations.repository.RepositoryCours
import fr.fabien.jpa.cotations.repository.RepositoryValeur
import fr.fabien.jpa.cotations.repository.abcbourse.RepositoryAbcCotation
import fr.fabien.jpa.cotations.repository.abcbourse.RepositoryAbcLibelle
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
 * Les DataSources sont mockées avec un H2 monté en mémoire pour la durée des tests (voir @Profile("!test")).
 *
 * Le reste n'est pas mocké et les tests sont exécutés dans l'ordre suivant :
 * 1 - récupération des libellés depuis ABCBourse
 * 2 - récupération des cotations depuis ABCBourse
 * 3 - conversion des libellés/cotations en valeurs/cours
 * 4 - calcul des moyennes mobiles
 */
@ActiveProfiles("test")
@SpringBatchTest
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class TestConfigurationAspirateur(
    @Autowired private val jobLauncherTestUtils: JobLauncherTestUtils,

    @Autowired private val jobMajAbcLibelles: Job,
    @Autowired private val jobMajAbcCotations: Job,
    @Autowired private val jobAbcToValeurCours: Job,
    @Autowired private val jobCalculerMoyennes: Job,

    @Autowired private val repositoryAbcLibelle: RepositoryAbcLibelle,
    @Autowired private val repositoryAbcCotation: RepositoryAbcCotation,
    @Autowired private val repositoryValeur: RepositoryValeur,
    @Autowired private val repositoryCours: RepositoryCours
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
    fun `Given jobMajAbcLibelles when launch job then there are libelles in repository (H2)`() {
        jobLauncherTestUtils.setJob(jobMajAbcLibelles)
        val jobExecution: JobExecution = jobLauncherTestUtils.launchJob(jobParameters)
        Assertions.assertEquals(ExitStatus.COMPLETED, jobExecution.exitStatus)
        Assertions.assertTrue(repositoryAbcLibelle.count() > 0, "aucun libellé récupéré depuis ABC bourse !")
    }

    @Test
    @Order(2)
    @Throws(Exception::class)
    fun `Given jobMajAbcCotations when launch job then there are cotations in repository (H2)`() {
        jobLauncherTestUtils.setJob(jobMajAbcCotations)
        val jobExecution: JobExecution = jobLauncherTestUtils.launchJob(jobParameters)
        Assertions.assertEquals(ExitStatus.COMPLETED, jobExecution.exitStatus)
        Assertions.assertTrue(repositoryAbcCotation.count() > 0, "aucune cotation récupéré depuis ABC bourse !")
    }

    @Test
    @Order(3)
    @Throws(Exception::class)
    fun `Given jobAbcToValeurCours when launch job then there are valeurs and cours in repository (H2)`() {
        jobLauncherTestUtils.setJob(jobAbcToValeurCours)
        val jobExecution: JobExecution = jobLauncherTestUtils.launchJob(jobParameters)
        Assertions.assertEquals(ExitStatus.COMPLETED, jobExecution.exitStatus)
        Assertions.assertTrue(repositoryValeur.count() > 0, "aucune valeur !")
        Assertions.assertTrue(repositoryCours.count() > 0, "aucun cours !")
    }

    @Test
    @Order(4)
    @Throws(Exception::class)
    fun `Given jobCalculerMoyennes when launch job then there are moyennes mobiles in repository (H2)`() {
        jobLauncherTestUtils.setJob(jobCalculerMoyennes)
        val jobExecution: JobExecution = jobLauncherTestUtils.launchJob(jobParameters)
        Assertions.assertEquals(ExitStatus.COMPLETED, jobExecution.exitStatus)
        repositoryCours.queryJoinValeur().forEach { cours ->
            Assertions.assertTrue(
                cours.moyennesMobiles.size > 0,
                "la moyenne mobile n'a pas été calculée pour le ticker ${cours.valeur.ticker}"
            )
        }
    }
}