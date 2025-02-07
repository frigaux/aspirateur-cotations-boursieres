package fr.fabien.aspirateur.cotations.job.step.business.tasklet

import fr.fabien.aspirateur.cotations.repository.RepositoryCours
import org.springframework.batch.core.StepContribution
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope("singleton")
class TaskletCalculerIndicateurs(
    private val repositoryCours: RepositoryCours
) : Tasklet {

    override fun execute(contribution: StepContribution, chunkContext: ChunkContext): RepeatStatus? {
        TODO("moyennesMobiles: stocker les moyennes mobiles en JSON")
        return RepeatStatus.FINISHED
    }
}