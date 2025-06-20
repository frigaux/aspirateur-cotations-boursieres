package fr.fabien.aspirateur.cotations.job.step.boursorama.reader

import fr.fabien.aspirateur.cotations.dto.boursorama.DtoBoursoramaCours
import fr.fabien.aspirateur.cotations.job.step.boursorama.tasklet.TaskletRecupererBoursoramaCours
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
import org.springframework.core.io.FileUrlResource
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Component
@Scope("singleton")
class ReaderBoursoramaCours : ItemReader<DtoBoursoramaCours> {
    companion object {
        lateinit var executionContext: ExecutionContext
        val reader: FlatFileItemReader<DtoBoursoramaCours> by lazy {
            FlatFileItemReaderBuilder<DtoBoursoramaCours>()
                .name("readerCours")
                .resource(ByteArrayResource(executionContext.get(TaskletRecupererBoursoramaCours.CSV) as ByteArray))
                .encoding(Charsets.UTF_8.name())
                .delimited()
                .delimiter("\t")
                .names("ticker", "nom", "date", "ouverture", "plusHaut", "plusBas", "cloture", "volume", "devise", "marche")
                .fieldSetMapper(fieldSetMapperDtoBoursorama())
                .build()
                .also { it.open(ExecutionContext()) }
        }

        private fun fieldSetMapperDtoBoursorama(): RecordFieldSetMapper<DtoBoursoramaCours> {
            val conversionServiceDtoCotation: DefaultConversionService = DefaultConversionService()
                .also {
                    val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                    it.addConverter(object : Converter<String, LocalDate> {
                        override fun convert(source: String): LocalDate {
                            return LocalDate.parse(source, formatter)
                        }
                    })
                }

            return RecordFieldSetMapper<DtoBoursoramaCours>(
                DtoBoursoramaCours::class.java, conversionServiceDtoCotation
            )
        }
    }

    @BeforeStep
    private fun beforeStep(stepExecution: StepExecution) {
        executionContext = stepExecution.jobExecution.executionContext
    }

    override fun read(): DtoBoursoramaCours? {
        return reader.read()
    }
}