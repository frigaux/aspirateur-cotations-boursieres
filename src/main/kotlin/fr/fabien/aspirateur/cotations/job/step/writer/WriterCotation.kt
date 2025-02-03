package fr.fabien.aspirateur.cotations.job.step.writer

import fr.fabien.aspirateur.cotations.ApplicationAspirateur
import fr.fabien.aspirateur.cotations.dto.DtoCotation
import fr.fabien.aspirateur.cotations.entity.Cotation
import fr.fabien.aspirateur.cotations.entity.Libelle
import fr.fabien.aspirateur.cotations.repository.RepositoryLibelle
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
class WriterCotation(private val repositoryLibelle: RepositoryLibelle) : ItemWriter<DtoCotation> {
    companion object {
        var stepExecution: StepExecution? = null
        val date: LocalDate by lazy {
            stepExecution!!.jobParameters.getLocalDate(ApplicationAspirateur.DATE)!!
        }
    }

    @BeforeStep
    fun beforeStep(stepExecution: StepExecution) {
        WriterCotation.stepExecution = stepExecution
    }

    // cette méthode est appelée dans une transaction
    // 1 - la query charge les libellés avec un fetch sur la cotation dans la session
    // 2 - si pas d'id lors du save -> insert
    //     si id lors du save -> utilisation de la session pour savoir un update est nécessaire sans SELECT supplémentaire
    override fun write(dtoCotations: Chunk<out DtoCotation>) {
        val tickers: List<String> = dtoCotations.map { it.ticker }
        val libelleByTicker: Map<String, Libelle> = repositoryLibelle.queryByDateAndTickerIn(date, tickers)
            .associateBy({ it.ticker }, { it })
        for (dtoCotation in dtoCotations) {
            if (dtoCotation.date != date) { // vérification date du DTO = date de la requête = date passée en paramètre du job
                throw UnexpectedJobExecutionException("La date de la cotation ${dtoCotation.date} ne correspond pas à la date passée en paramètre $date")
            } else {
                libelleByTicker[dtoCotation.ticker]?.let {
                    it.cotation?.let {
                        it.ouverture = dtoCotation.ouverture
                        it.plusHaut = dtoCotation.plusHaut
                        it.plusBas = dtoCotation.plusBas
                        it.cloture = dtoCotation.cloture
                        it.volume = dtoCotation.volume
                        it
                    } ?: run {
                        it.cotation = Cotation(
                            dtoCotation.ouverture,
                            dtoCotation.plusHaut,
                            dtoCotation.plusBas,
                            dtoCotation.cloture,
                            dtoCotation.volume
                        )
                    }
                    repositoryLibelle.save(it)
                } ?: run {
                    throw UnexpectedJobExecutionException("Impossible de persister la cotation car le libellé est manquant pour le ticker ${dtoCotation.ticker} à la date $date")
                }
            }
        }
    }
}