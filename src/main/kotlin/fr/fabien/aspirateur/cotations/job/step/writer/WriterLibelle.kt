package fr.fabien.aspirateur.cotations.configuration.step.writer

import fr.fabien.aspirateur.cotations.configuration.step.tasklet.TaskletRecupererLibelles
import fr.fabien.aspirateur.cotations.dto.DtoLibelle
import fr.fabien.aspirateur.cotations.entity.Libelle
import fr.fabien.aspirateur.cotations.repository.LibelleRepository
import org.springframework.batch.core.StepExecution
import org.springframework.batch.core.annotation.BeforeStep
import org.springframework.batch.item.Chunk
import org.springframework.batch.item.ExecutionContext
import org.springframework.batch.item.ItemWriter
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Component
@Scope("singleton")
class WriterLibelle(private val libelleRepository: LibelleRepository) : ItemWriter<DtoLibelle> {
    companion object {
        var executionContext: ExecutionContext? = null
        var date: LocalDate? = null
        var idByIsin: Map<String, Int>? = null
    }

    @BeforeStep
    fun beforeStep(stepExecution: StepExecution) {
        executionContext = stepExecution.jobExecution.executionContext
        date = executionContext!!.get(TaskletRecupererLibelles.DATE) as LocalDate
        idByIsin = libelleRepository.findByDate(date!!).associateBy({ it.isin }, { it.id!! })
    }

    @Transactional
    override fun write(dtoLibelles: Chunk<out DtoLibelle>) {
        val date: LocalDate = executionContext!!.get(TaskletRecupererLibelles.DATE) as LocalDate
        for (dtoLibelle in dtoLibelles) {
            val entity: Libelle = Libelle(date, dtoLibelle.isin, dtoLibelle.ticker, dtoLibelle.nom, idByIsin?.get(dtoLibelle.isin))
            // no id : insert
            // id : select and then update (detached entity)
            libelleRepository.save(entity)
        }
    }
}