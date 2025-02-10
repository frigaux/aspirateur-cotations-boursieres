package fr.fabien.aspirateur.cotations.job.step.business.tasklet

import fr.fabien.aspirateur.cotations.ApplicationAspirateur
import fr.fabien.aspirateur.cotations.entity.Cours
import fr.fabien.aspirateur.cotations.entity.Valeur
import fr.fabien.aspirateur.cotations.repository.RepositoryCours
import org.springframework.batch.core.StepContribution
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
@Scope("singleton")
class TaskletCalculerMoyennes(
    private val repositoryCours: RepositoryCours
) : Tasklet {

    override fun execute(contribution: StepContribution, chunkContext: ChunkContext): RepeatStatus? {
        val date: LocalDate = contribution.stepExecution.jobParameters.getLocalDate(ApplicationAspirateur.DATE)!!
        repositoryCours.findByDate(date)
            .forEach { cours -> calculerMoyennesMobiles(cours.valeur, date) }
        return RepeatStatus.FINISHED
    }

    private fun calculerMoyennesMobiles(valeur: Valeur, date: LocalDate) {
        val lesCours: List<Cours> = repositoryCours.query300BeforeDate(valeur, date)
    }
}