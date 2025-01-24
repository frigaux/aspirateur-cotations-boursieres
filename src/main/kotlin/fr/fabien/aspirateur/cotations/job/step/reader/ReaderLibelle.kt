package fr.fabien.aspirateur.cotations.configuration.step.reader

import fr.fabien.aspirateur.cotations.configuration.step.tasklet.TaskletRecupererLibelles
import fr.fabien.aspirateur.cotations.dto.DtoLibelle
import fr.fabien.aspirateur.cotations.repository.LibelleRepository
import org.springframework.batch.core.StepExecution
import org.springframework.batch.core.annotation.BeforeStep
import org.springframework.batch.item.ExecutionContext
import org.springframework.batch.item.ItemReader
import org.springframework.batch.item.file.FlatFileItemReader
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder
import org.springframework.context.annotation.Scope
import org.springframework.core.io.ByteArrayResource
import org.springframework.stereotype.Component

@Component
@Scope("singleton")
class ReaderLibelle(private val libelleRepository: LibelleRepository) : ItemReader<DtoLibelle> {
    companion object {
        var executionContext: ExecutionContext? = null
        val reader: FlatFileItemReader<DtoLibelle> by lazy {
            FlatFileItemReaderBuilder<DtoLibelle>()
                .name("readerLibelle")
                .resource(ByteArrayResource(executionContext!!.get(TaskletRecupererLibelles.CSV) as ByteArray))
                .encoding(executionContext!!.getString(TaskletRecupererLibelles.ENCODING))
                .delimited()
                .delimiter(";")
                .names("isin", "nom", "ticker")
                .targetType(DtoLibelle::class.java)
                .linesToSkip(1)
                .build()
                .also { it.open(ExecutionContext()) }
        }
    }

    @BeforeStep
    fun beforeStep(stepExecution: StepExecution) {
        executionContext = stepExecution.jobExecution.executionContext
    }

    override fun read(): DtoLibelle? {
        return reader.read()
    }
}