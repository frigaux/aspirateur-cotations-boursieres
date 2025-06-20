package fr.fabien.aspirateur.cotations.job.step.boursorama.writer

import fr.fabien.aspirateur.cotations.ApplicationAspirateur
import fr.fabien.aspirateur.cotations.dto.boursorama.DtoBoursoramaCours
import fr.fabien.jpa.cotations.entity.boursorama.BoursoramaCours
import fr.fabien.jpa.cotations.repository.boursorama.RepositoryBoursoramaCours
import org.springframework.batch.core.StepExecution
import org.springframework.batch.core.UnexpectedJobExecutionException
import org.springframework.batch.core.annotation.BeforeStep
import org.springframework.batch.item.Chunk
import org.springframework.batch.item.ItemWriter
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
@Scope("singleton")
class WriterBoursoramaCours(private val repositoryBoursoramaCours: RepositoryBoursoramaCours) :
    ItemWriter<DtoBoursoramaCours> {
    companion object {
        lateinit var stepExecution: StepExecution
        val date: LocalDate by lazy {
            stepExecution.jobParameters.getLocalDate(ApplicationAspirateur.DATE)!!
        }
    }

    @BeforeStep
    private fun beforeStep(stepExecution: StepExecution) {
        Companion.stepExecution = stepExecution
    }

    // cette méthode est appelée dans une transaction
    // 1 - la query charge les cours
    // 2 - si pas d'id lors du save -> insert
    //     si id lors du save -> utilisation de la session pour savoir un update est nécessaire sans SELECT supplémentaire
    override fun write(dtoBoursoramaCours: Chunk<out DtoBoursoramaCours>) {
        val tickers: List<String> = dtoBoursoramaCours.map { it.ticker }
        val coursByTicker: Map<String, BoursoramaCours> = repositoryBoursoramaCours.findByDateAndTickerIn(date, tickers)
            .associateBy { it.ticker }
        for (dtoBoursoramaCours in dtoBoursoramaCours) {
            if (dtoBoursoramaCours.date != date) { // vérification date du DTO = date de la requête = date passée en paramètre du job
                throw UnexpectedJobExecutionException("La date du cours ${dtoBoursoramaCours.date} ne correspond pas à la date passée en paramètre $date")
            } else {
                repositoryBoursoramaCours.save(coursByTicker[dtoBoursoramaCours.ticker]?.apply {
                    nom = dtoBoursoramaCours.nom
                    ouverture = dtoBoursoramaCours.ouverture
                    plusHaut = dtoBoursoramaCours.plusHaut
                    plusBas = dtoBoursoramaCours.plusBas
                    cloture = dtoBoursoramaCours.cloture
                    volume = dtoBoursoramaCours.volume
                    devise = dtoBoursoramaCours.devise
                } ?: run {
                    BoursoramaCours(
                        dtoBoursoramaCours.marche,
                        dtoBoursoramaCours.ticker,
                        dtoBoursoramaCours.nom,
                        dtoBoursoramaCours.date,
                        dtoBoursoramaCours.ouverture,
                        dtoBoursoramaCours.plusHaut,
                        dtoBoursoramaCours.plusBas,
                        dtoBoursoramaCours.cloture,
                        dtoBoursoramaCours.volume,
                        dtoBoursoramaCours.devise
                    )
                })
            }
        }
    }
}