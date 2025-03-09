package fr.fabien.aspirateur.cotations.job.step.abcbourse.reader

import fr.fabien.aspirateur.cotations.dto.abcbourse.DtoAbcCotation
import fr.fabien.aspirateur.cotations.job.step.abcbourse.tasklet.TaskletRecupererAbcCotations
import org.springframework.batch.core.StepExecution
import org.springframework.batch.core.annotation.BeforeStep
import org.springframework.batch.item.ExecutionContext
import org.springframework.batch.item.ItemReader
import org.springframework.batch.item.file.FlatFileItemReader
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder
import org.springframework.batch.item.file.mapping.RecordFieldSetMapper
import org.springframework.context.annotation.Scope
import org.springframework.core.convert.converter.Converter
import org.springframework.core.convert.support.DefaultConversionService
import org.springframework.core.io.ByteArrayResource
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Component
@Scope("singleton")
class ReaderAbcCotation : ItemReader<DtoAbcCotation> {
    companion object {
        lateinit var executionContext: ExecutionContext
        val reader: FlatFileItemReader<DtoAbcCotation> by lazy {
            FlatFileItemReaderBuilder<DtoAbcCotation>()
                .name("readerCotation")
                .resource(ByteArrayResource(executionContext.get(TaskletRecupererAbcCotations.CSV) as ByteArray))
                .encoding(executionContext.getString(TaskletRecupererAbcCotations.CHARSET))
                .delimited()
                .delimiter(";")
                .names("ticker", "date", "ouverture", "plusHaut", "plusBas", "cloture", "volume")
                .fieldSetMapper(fieldSetMapperDtoCotation())
                .build()
                .also { it.open(ExecutionContext()) }
        }

        private fun fieldSetMapperDtoCotation(): RecordFieldSetMapper<DtoAbcCotation> {
            val conversionServiceDtoCotation: DefaultConversionService = DefaultConversionService()
                .also {
                    val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yy")
                    it.addConverter(object : Converter<String, LocalDate> {
                        override fun convert(source: String): LocalDate {
                            return LocalDate.parse(source, formatter)
                        }
                    })
                }

            return RecordFieldSetMapper<DtoAbcCotation>(
                DtoAbcCotation::class.java, conversionServiceDtoCotation)
        }
    }

    @BeforeStep
    private fun beforeStep(stepExecution: StepExecution) {
        executionContext = stepExecution.jobExecution.executionContext
    }

    override fun read(): DtoAbcCotation? {
        return reader.read()
    }
}