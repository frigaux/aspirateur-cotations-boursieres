package fr.fabien.aspirateur.cotations.job.step.business.tasklet

import fr.fabien.aspirateur.cotations.ApplicationAspirateur
import fr.fabien.aspirateur.cotations.entity.abcbourse.AbcLibelle
import fr.fabien.aspirateur.cotations.entity.Cours
import fr.fabien.aspirateur.cotations.entity.Valeur
import fr.fabien.aspirateur.cotations.repository.RepositoryAbcLibelle
import fr.fabien.aspirateur.cotations.repository.RepositoryCours
import fr.fabien.aspirateur.cotations.repository.RepositoryValeur
import org.springframework.batch.core.StepContribution
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
@Scope("singleton")
class TaskletAbcToValeurCours(
    private val repositoryAbcLibelle: RepositoryAbcLibelle,
    private val repositoryValeur: RepositoryValeur,
    private val repositoryCours: RepositoryCours
) : Tasklet {

    override fun execute(contribution: StepContribution, chunkContext: ChunkContext): RepeatStatus? {
        val date: LocalDate = contribution.stepExecution.jobParameters.getLocalDate(ApplicationAspirateur.DATE)!!
        val abcLibelles: List<AbcLibelle> = repositoryAbcLibelle.queryByDate(date)
        val valeurByTicker: Map<String, Valeur> = repositoryValeur.queryByDate(date)
            .associateBy { it.ticker }
        for (abcLibelle in abcLibelles) {
            val valeur = valeurByTicker[abcLibelle.ticker]?.also { valeur ->
                valeur.libelle = abcLibelle.nom
            } ?: run {
                Valeur(abcLibelle.ticker, abcLibelle.marche, abcLibelle.nom, setOf())
            }
            repositoryValeur.save(valeur)

            abcLibelle.abcCotation?.let { cotation ->
                if (valeur.cours.isEmpty()) {
                    val cours: Cours = Cours(
                        valeur,
                        abcLibelle.date,
                        cotation.ouverture,
                        cotation.plusHaut,
                        cotation.plusBas,
                        cotation.cloture,
                        cotation.volume,
                        null,
                        null
                    )
                    repositoryCours.save(cours)
                } else {
                    val cours: Cours = valeur.cours.elementAt(0)
                    cours.ouverture = cotation.ouverture
                    cours.plusHaut = cotation.plusHaut
                    cours.plusBas = cotation.plusBas
                    cours.cloture = cotation.cloture
                    cours.volume = cotation.volume
                    repositoryCours.save(cours)
                }
            }
        }

        return RepeatStatus.FINISHED
    }
}