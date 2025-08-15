package fr.fabien.aspirateur.cotations.job.step.business.tasklet

import fr.fabien.aspirateur.cotations.ApplicationAspirateur
import fr.fabien.jpa.cotations.entity.Cours
import fr.fabien.jpa.cotations.entity.Valeur
import fr.fabien.jpa.cotations.repository.RepositoryCours
import fr.fabien.jpa.cotations.repository.RepositoryValeur
import mu.KotlinLogging
import org.springframework.batch.core.StepContribution
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate

@Component
@Scope("singleton")
class TaskletCalculerMoyennes(
    private val repositoryValeur: RepositoryValeur,
    private val repositoryCours: RepositoryCours
) : Tasklet {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun execute(contribution: StepContribution, chunkContext: ChunkContext): RepeatStatus? {
        val date: LocalDate = contribution.stepExecution.jobParameters.getLocalDate(ApplicationAspirateur.DATE)!!
        val valeurs: List<Valeur> = repositoryValeur.queryJoinCoursByDate(date)
        logger.info { "Nombre de cours le $date : ${valeurs.size}" }
        valeurs.forEach { valeur -> calculerMoyennesMobiles(valeur, date) }
        return RepeatStatus.FINISHED
    }

    private fun calculerMoyennesMobiles(valeur: Valeur, date: LocalDate) {
        var count = 0
        var sum = BigDecimal(0)
        val lesCours: List<Cours> = repositoryCours.queryBeforeDateByValeur(valeur, date, 300)
        val coursALaDate = lesCours[0]
        lesCours.forEach { cours ->
            count++
            sum = sum.plus(BigDecimal(cours.cloture))
            coursALaDate.moyennesMobiles.add(sum.divide(BigDecimal(count), 5, RoundingMode.HALF_UP).toDouble())
        }
        repositoryCours.save(coursALaDate)
    }
}