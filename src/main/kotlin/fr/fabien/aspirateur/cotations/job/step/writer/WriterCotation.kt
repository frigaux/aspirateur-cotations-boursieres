package fr.fabien.aspirateur.cotations.configuration.step.writer

import fr.fabien.aspirateur.cotations.dto.DtoCotation
import fr.fabien.aspirateur.cotations.repository.RepositoryCotation
import org.springframework.batch.core.StepExecution
import org.springframework.batch.core.annotation.BeforeStep
import org.springframework.batch.item.Chunk
import org.springframework.batch.item.ExecutionContext
import org.springframework.batch.item.ItemWriter
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope("singleton")
class WriterCotation(private val repositoryCotation: RepositoryCotation) : ItemWriter<DtoCotation> {
    companion object {
        var executionContext: ExecutionContext? = null
    }

    @BeforeStep
    fun beforeStep(stepExecution: StepExecution) {
        executionContext = stepExecution.jobExecution.executionContext
    }

    // cette méthode est appelée dans une transaction
    // 1 - le find charge les entités dans la session
    // 2 - si pas d'id lors du save -> insert
    //     si id lors du save -> la session est utilisée pour savoir si l'entité a été modifiée -> update
    override fun write(dtoCotations: Chunk<out DtoCotation>) {
        val tickers: List<String> = dtoCotations.map { it.ticker }
//        val libelleByTicker: Map<String, Libelle> = repositoryLibelle.findByDateAndTickerIn(date!!, tickers)
//            .associateBy({ it.ticker }, { it })
//        for (dtoLibelle in dtoLibelles) {
//            val entity: Libelle = libelleByTicker[dtoLibelle.ticker]?.let {
//                it.isin = dtoLibelle.isin
//                it.nom = dtoLibelle.nom
//                it
//            } ?: run {
//                Libelle(date!!, dtoLibelle.ticker, dtoLibelle.isin, dtoLibelle.nom)
//            }
//            repositoryLibelle.save(entity)
//        }
    }
}