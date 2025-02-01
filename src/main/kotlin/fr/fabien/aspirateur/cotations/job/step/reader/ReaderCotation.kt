package fr.fabien.aspirateur.cotations.job.step.reader

import fr.fabien.aspirateur.cotations.job.step.tasklet.TaskletRecupererCotations
import fr.fabien.aspirateur.cotations.dto.DtoCotation
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
class ReaderCotation: ItemReader<DtoCotation> {
    companion object {
        var executionContext: ExecutionContext? = null
        val reader: FlatFileItemReader<DtoCotation> by lazy {
            FlatFileItemReaderBuilder<DtoCotation>()
                .name("readerCotation")
                .resource(ByteArrayResource(executionContext!!.get(TaskletRecupererCotations.CSV) as ByteArray))
                .encoding(executionContext!!.getString(TaskletRecupererCotations.CHARSET))
                .delimited()
                .delimiter(";")
                .names("date", "ticker", "ouverture", "plusHaut", "plusBas", "cloture", "volume")
                .targetType(DtoCotation::class.java)
//                .fieldSetMapper(go())
                .build()
                .also { it.open(ExecutionContext()) }
        }

//        private fun go(): FieldSetMapper<DtoCotation> {
//            val conversionServiceDtoCotation: DefaultConversionService = DefaultConversionService()
//            val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("")
//            conversionServiceDtoCotation.addConverter(object : Converter<String, LocalDate> {
//                override fun convert(source: String): LocalDate {
//                    return LocalDate.parse(source, formatter)
//                }
//            })
//            val mapper: BeanWrapperFieldSetMapper<DtoCotation> = BeanWrapperFieldSetMapper<DtoCotation>()
//            mapper.setConversionService(conversionServiceDtoCotation)
//            mapper.setTargetType(DtoCotation::class.java)
//            return mapper
//        }
    }

    @BeforeStep
    fun beforeStep(stepExecution: StepExecution) {
        executionContext = stepExecution.jobExecution.executionContext
    }

    override fun read(): DtoCotation? {
        return reader.read()
    }
}