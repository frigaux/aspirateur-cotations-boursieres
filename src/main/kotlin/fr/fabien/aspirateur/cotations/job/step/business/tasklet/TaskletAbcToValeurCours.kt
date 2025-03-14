package fr.fabien.aspirateur.cotations.job.step.business.tasklet

import fr.fabien.aspirateur.cotations.ApplicationAspirateur
import fr.fabien.jpa.cotations.entity.Cours
import fr.fabien.jpa.cotations.entity.Valeur
import fr.fabien.jpa.cotations.entity.abcbourse.AbcLibelle
import fr.fabien.jpa.cotations.repository.abcbourse.RepositoryAbcLibelle
import fr.fabien.jpa.cotations.repository.RepositoryCours
import fr.fabien.jpa.cotations.repository.RepositoryValeur
import mu.KotlinLogging
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

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun execute(contribution: StepContribution, chunkContext: ChunkContext): RepeatStatus? {
        val date: LocalDate = contribution.stepExecution.jobParameters.getLocalDate(ApplicationAspirateur.DATE)!!
        val abcLibelles: List<AbcLibelle> = repositoryAbcLibelle.queryByDate(date)
        logger.info { "Nombre de libellés récupérés sur ABCBourse le $date : ${abcLibelles.size}"}
        val nbCotations: Int = abcLibelles.filter { abcLibelle: AbcLibelle -> abcLibelle.abcCotation != null }.size
        logger.info { "Nombre de cotations récupérés sur ABCBourse le $date : $nbCotations"}
        val valeurByTicker: Map<String, Valeur> = repositoryValeur
            .findAll()
            .associateBy { it.ticker }
        repositoryValeur.queryJoinCoursByDate(date)
            .forEach { valeur -> valeurByTicker.plus(Pair(valeur.ticker, valeur)) }
        for (abcLibelle in abcLibelles) {
            val valeur = valeurByTicker[abcLibelle.ticker]?.also { valeur ->
                valeur.marche = abcLibelle.marche
                valeur.libelle = abcLibelle.nom
            } ?: run {
                Valeur(abcLibelle.ticker, abcLibelle.marche, abcLibelle.nom, setOf())
            }
            repositoryValeur.save(valeur)

            abcLibelle.abcCotation?.let { cotation ->
                val cours = Cours(
                    valeur,
                    abcLibelle.date,
                    cotation.ouverture,
                    cotation.plusHaut,
                    cotation.plusBas,
                    cotation.cloture,
                    cotation.volume,
                    mutableListOf(),
                    false
                )
                repositoryCours.save(cours)
            }
        }

        return RepeatStatus.FINISHED
    }
}