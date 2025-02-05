package fr.fabien.aspirateur.cotations.job.step.writer

import fr.fabien.aspirateur.cotations.ApplicationAspirateur
import fr.fabien.aspirateur.cotations.dto.DtoAbcCotation
import fr.fabien.aspirateur.cotations.entity.AbcCotation
import fr.fabien.aspirateur.cotations.entity.AbcLibelle
import fr.fabien.aspirateur.cotations.repository.RepositoryAbcLibelle
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
class WriterAbcCotation(private val repositoryAbcLibelle: RepositoryAbcLibelle) : ItemWriter<DtoAbcCotation> {
    companion object {
        var stepExecution: StepExecution? = null
        val date: LocalDate by lazy {
            stepExecution!!.jobParameters.getLocalDate(ApplicationAspirateur.DATE)!!
        }
    }

    @BeforeStep
    fun beforeStep(stepExecution: StepExecution) {
        WriterAbcCotation.stepExecution = stepExecution
    }

    // cette méthode est appelée dans une transaction
    // 1 - la query charge les libellés avec un fetch sur la cotation dans la session
    // 2 - si pas d'id lors du save -> insert
    //     si id lors du save -> utilisation de la session pour savoir un update est nécessaire sans SELECT supplémentaire
    override fun write(dtoAbcCotations: Chunk<out DtoAbcCotation>) {
        val tickers: List<String> = dtoAbcCotations.map { it.ticker }
        val abcLibelleByTicker: Map<String, AbcLibelle> = repositoryAbcLibelle.queryByDateAndTickerIn(date, tickers)
            .associateBy({ it.ticker }, { it })
        for (dtoAbcCotation in dtoAbcCotations) {
            if (dtoAbcCotation.date != date) { // vérification date du DTO = date de la requête = date passée en paramètre du job
                throw UnexpectedJobExecutionException("La date de la cotation ${dtoAbcCotation.date} ne correspond pas à la date passée en paramètre $date")
            } else {
                abcLibelleByTicker[dtoAbcCotation.ticker]?.let {
                    it.abcCotation?.let {
                        it.ouverture = dtoAbcCotation.ouverture
                        it.plusHaut = dtoAbcCotation.plusHaut
                        it.plusBas = dtoAbcCotation.plusBas
                        it.cloture = dtoAbcCotation.cloture
                        it.volume = dtoAbcCotation.volume
                        it
                    } ?: run {
                        it.abcCotation = AbcCotation(
                            dtoAbcCotation.ouverture,
                            dtoAbcCotation.plusHaut,
                            dtoAbcCotation.plusBas,
                            dtoAbcCotation.cloture,
                            dtoAbcCotation.volume
                        )
                    }
                    repositoryAbcLibelle.save(it)
                } ?: run {
                    throw UnexpectedJobExecutionException("Impossible de persister la cotation car le libellé est manquant pour le ticker ${dtoAbcCotation.ticker} à la date $date")
                }
            }
        }
    }
}