package fr.fabien.aspirateur.cotations.job.step.business.tasklet

import fr.fabien.aspirateur.cotations.ApplicationAspirateur
import fr.fabien.jpa.cotations.entity.Cours
import fr.fabien.jpa.cotations.entity.Valeur
import fr.fabien.jpa.cotations.repository.RepositoryCours
import fr.fabien.jpa.cotations.repository.RepositoryValeur
import org.springframework.batch.core.StepContribution
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.LocalDate

@Component
@Scope("singleton")
class TaskletCalculerMoyennes(
    private val repositoryValeur: RepositoryValeur,
    private val repositoryCours: RepositoryCours
) : Tasklet {

    override fun execute(contribution: StepContribution, chunkContext: ChunkContext): RepeatStatus? {
        val date: LocalDate = contribution.stepExecution.jobParameters.getLocalDate(ApplicationAspirateur.DATE)!!
        repositoryValeur.queryJoinCoursByDate(date)
            .forEach { valeur -> calculerMoyennesMobiles(valeur, date) }
        return RepeatStatus.FINISHED
    }

    private fun calculerMoyennesMobiles(valeur: Valeur, date: LocalDate) {
        var count = 0
        var sum = BigDecimal(0)
        val lesCours: List<Cours> = repositoryCours.query300BeforeDate(valeur, date)
        val coursALaDate = lesCours[0]
        lesCours.forEach { cours ->
            count++
            sum = sum.plus(BigDecimal(cours.cloture))
            coursALaDate.moyennesMobiles.add(sum.divide(BigDecimal(count)).toDouble())
        }
        coursALaDate.alerte = determinerAlerte(lesCours)
        repositoryCours.save(coursALaDate)
    }

    private fun determinerAlerte(lesCours: List<Cours>): Boolean {
        if (lesCours.size > 4) {
            val cours1 = lesCours[0].cloture
            val diff5_3: Double = lesCours[4].cloture - lesCours[2].cloture
            val diff3_1: Double = lesCours[2].cloture - lesCours[0].cloture
            return ((diff5_3 >= diff3_1 && diff3_1 < cours1) || (diff5_3 < diff3_1 && diff3_1 >= cours1))
        }
        return false
    }
}