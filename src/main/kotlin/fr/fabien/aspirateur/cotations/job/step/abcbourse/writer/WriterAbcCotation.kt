package fr.fabien.aspirateur.cotations.job.step.abcbourse.writer

import fr.fabien.aspirateur.cotations.ApplicationAspirateur
import fr.fabien.aspirateur.cotations.dto.abcbourse.DtoAbcCotation
import fr.fabien.jpa.cotations.entity.abcbourse.AbcCotation
import fr.fabien.jpa.cotations.entity.abcbourse.AbcLibelle
import fr.fabien.jpa.cotations.repository.abcbourse.RepositoryAbcLibelle
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
    // 1 - la query charge les libellés avec un fetch sur la cotation dans la session
    // 2 - si pas d'id lors du save -> insert
    //     si id lors du save -> utilisation de la session pour savoir un update est nécessaire sans SELECT supplémentaire
    override fun write(dtoAbcCotations: Chunk<out DtoAbcCotation>) {
        val tickers: List<String> = dtoAbcCotations.map { it.ticker }
        val abcLibelleByTicker: Map<String, AbcLibelle> = repositoryAbcLibelle.queryByDateAndTickerIn(date, tickers)
            .associateBy { it.ticker }
        for (dtoAbcCotation in dtoAbcCotations) {
            if (dtoAbcCotation.date != date) { // vérification date du DTO = date de la requête = date passée en paramètre du job
                throw UnexpectedJobExecutionException("La date de la cotation ${dtoAbcCotation.date} ne correspond pas à la date passée en paramètre $date")
            } else {
                abcLibelleByTicker[dtoAbcCotation.ticker]?.also { libelle ->
                    libelle.abcCotation?.apply {
                        ouverture = dtoAbcCotation.ouverture
                        plusHaut = dtoAbcCotation.plusHaut
                        plusBas = dtoAbcCotation.plusBas
                        cloture = dtoAbcCotation.cloture
                        volume = dtoAbcCotation.volume
                    } ?: run {
                        libelle.abcCotation = AbcCotation(
                            dtoAbcCotation.ouverture,
                            dtoAbcCotation.plusHaut,
                            dtoAbcCotation.plusBas,
                            dtoAbcCotation.cloture,
                            dtoAbcCotation.volume
                        )
                    }
                    repositoryAbcLibelle.save(libelle)
                } ?: run {
                    throw UnexpectedJobExecutionException("Impossible de persister la cotation car le libellé est manquant pour le ticker ${dtoAbcCotation.ticker} à la date $date")
                }
            }
        }
    }
}