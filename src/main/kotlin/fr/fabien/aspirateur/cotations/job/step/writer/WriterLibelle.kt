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
import java.time.LocalDate

@Component
@Scope("singleton")
class WriterLibelle(private val libelleRepository: LibelleRepository) : ItemWriter<DtoLibelle> {
    companion object {
        var executionContext: ExecutionContext? = null
    }

    @BeforeStep
    fun beforeStep(stepExecution: StepExecution) {
        executionContext = stepExecution.jobExecution.executionContext
    }

    override fun write(libelles: Chunk<out DtoLibelle>) {
        val date : LocalDate = executionContext!!.get(TaskletRecupererLibelles.DATE) as LocalDate
        for (libelle in libelles) {
            val entity: Libelle = Libelle(date, libelle.isin, libelle.ticker, libelle.nom)
            libelleRepository.save(entity)
        }
    }
}