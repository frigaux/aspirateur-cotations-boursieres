package fr.fabien.aspirateur.cotations.job.step.abcbourse.writer

import fr.fabien.aspirateur.cotations.ApplicationAspirateur
import fr.fabien.aspirateur.cotations.dto.abcbourse.DtoAbcLibelle
import fr.fabien.jpa.cotations.entity.abcbourse.AbcLibelle
import fr.fabien.jpa.cotations.repository.abcbourse.RepositoryAbcLibelle
import org.springframework.batch.core.StepExecution
import org.springframework.batch.core.annotation.BeforeStep
import org.springframework.batch.item.Chunk
import org.springframework.batch.item.ItemWriter
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
@Scope("singleton")
class WriterAbcLibelle(private val repositoryAbcLibelle: RepositoryAbcLibelle) : ItemWriter<DtoAbcLibelle> {
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
    // 1 - le find charge les libellés dans la session
    // 2 - si pas d'id lors du save -> insert
    //     si id lors du save -> utilisation de la session pour savoir un update est nécessaire sans SELECT supplémentaire
    override fun write(dtoAbcLibelles: Chunk<out DtoAbcLibelle>) {
        val tickers: List<String> = dtoAbcLibelles.map { it.ticker }
        val abcLibelleByTicker: Map<String, AbcLibelle> = repositoryAbcLibelle.findByDateAndTickerIn(date, tickers)
            .associateBy { it.ticker }
        for (dtoAbcLibelle in dtoAbcLibelles) {
            val libelle: AbcLibelle = abcLibelleByTicker[dtoAbcLibelle.ticker]?.apply {
                isin = dtoAbcLibelle.isin
                nom = dtoAbcLibelle.nom
            } ?: run {
                AbcLibelle(date, dtoAbcLibelle.ticker, dtoAbcLibelle.isin, dtoAbcLibelle.marche, dtoAbcLibelle.nom)
            }
            repositoryAbcLibelle.save(libelle)
        }
    }
}