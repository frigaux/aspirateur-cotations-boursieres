package fr.fabien.aspirateur.cotations.job.step.writer

import fr.fabien.aspirateur.cotations.ApplicationAspirateur
import fr.fabien.aspirateur.cotations.dto.DtoLibelle
import fr.fabien.aspirateur.cotations.entity.Libelle
import fr.fabien.aspirateur.cotations.repository.RepositoryLibelle
import org.springframework.batch.core.StepExecution
import org.springframework.batch.core.annotation.BeforeStep
import org.springframework.batch.item.Chunk
import org.springframework.batch.item.ItemWriter
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
@Scope("singleton")
class WriterLibelle(private val repositoryLibelle: RepositoryLibelle) : ItemWriter<DtoLibelle> {
    companion object {
        var stepExecution: StepExecution? = null
        val date: LocalDate by lazy {
            stepExecution!!.jobParameters.getLocalDate(ApplicationAspirateur.DATE)!!
        }
    }

    @BeforeStep
    fun beforeStep(stepExecution: StepExecution) {
        Companion.stepExecution = stepExecution
    }

    // cette méthode est appelée dans une transaction
    // 1 - le find charge les entités dans la session
    // 2 - si pas d'id lors du save -> insert
    //     si id lors du save -> la session est utilisée pour savoir si l'entité a été modifiée -> update
    override fun write(dtoLibelles: Chunk<out DtoLibelle>) {
        val tickers: List<String> = dtoLibelles.map { it.ticker }
        val libelleByTicker: Map<String, Libelle> = repositoryLibelle.findByDateAndTickerIn(date!!, tickers)
            .associateBy({ it.ticker }, { it })
        for (dtoLibelle in dtoLibelles) {
            val entity: Libelle = libelleByTicker[dtoLibelle.ticker]?.let {
                it.isin = dtoLibelle.isin
                it.nom = dtoLibelle.nom
                it
            } ?: run {
                Libelle(date!!, dtoLibelle.ticker, dtoLibelle.isin, dtoLibelle.nom)
            }
            repositoryLibelle.save(entity)
        }
    }
}