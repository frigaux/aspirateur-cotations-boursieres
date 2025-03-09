package fr.fabien.aspirateur.cotations.job.step.abcbourse.reader

import fr.fabien.aspirateur.cotations.job.step.abcbourse.tasklet.TaskletRecupererAbcLibelles
import fr.fabien.aspirateur.cotations.dto.abcbourse.DtoAbcLibelle
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
class ReaderAbcLibelle : ItemReader<DtoAbcLibelle> {
    companion object {
        lateinit var executionContext: ExecutionContext
        val reader: FlatFileItemReader<DtoAbcLibelle> by lazy {
            FlatFileItemReaderBuilder<DtoAbcLibelle>()
                .name("readerLibelle")
                .resource(ByteArrayResource(executionContext.get(TaskletRecupererAbcLibelles.CSV) as ByteArray))
                .encoding(executionContext.getString(TaskletRecupererAbcLibelles.CHARSET))
                .delimited()
                .delimiter(";")
                .names("isin", "nom", "ticker", "marche")
                .targetType(DtoAbcLibelle::class.java)
                .build()
                .also { it.open(ExecutionContext()) }
        }
    }

    @BeforeStep
    private fun beforeStep(stepExecution: StepExecution) {
        executionContext = stepExecution.jobExecution.executionContext
    }

    override fun read(): DtoAbcLibelle? {
        return reader.read()
    }
}