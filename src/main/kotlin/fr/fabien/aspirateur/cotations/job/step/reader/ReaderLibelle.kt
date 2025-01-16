package fr.fabien.aspirateur.cotations.configuration.step.reader

import fr.fabien.aspirateur.cotations.configuration.step.tasklet.TaskletRecupererLibelles
import fr.fabien.aspirateur.cotations.job.dto.Libelle
import org.springframework.batch.item.ExecutionContext
import org.springframework.batch.item.ItemReader
import org.springframework.batch.item.file.FlatFileItemReader
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope("singleton")
class ReaderLibelle : ItemReader<Libelle> {
    companion object {
        val reader: FlatFileItemReader<Libelle> by lazy {
            FlatFileItemReaderBuilder<Libelle>()
                .name("readerLibelle")
                .resource(TaskletRecupererLibelles.csv!!)
                .encoding(TaskletRecupererLibelles.encoding!!.name())
                .delimited()
                .delimiter(";")
                .names("isin", "nom", "ticker")
                .targetType(Libelle::class.java)
                .linesToSkip(1)
                .build()
                .also {it.open(ExecutionContext())}
        }
    }

    override fun read(): Libelle? {
        return reader.read()
    }
}