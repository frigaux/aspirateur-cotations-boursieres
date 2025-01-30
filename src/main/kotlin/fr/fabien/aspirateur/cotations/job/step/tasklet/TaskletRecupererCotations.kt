package fr.fabien.aspirateur.cotations.configuration.step.tasklet

import fr.fabien.aspirateur.cotations.service.ServiceAbcBourse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.springframework.batch.core.StepContribution
import org.springframework.batch.core.UnexpectedJobExecutionException
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.item.ExecutionContext
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.core.io.ByteArrayResource
import org.springframework.stereotype.Component
import java.nio.charset.Charset
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.zip.GZIPInputStream

@Component
@Scope("singleton")
class TaskletRecupererCotations : Tasklet {
    @Autowired
    val serviceAbcBourse: ServiceAbcBourse? = null

    companion object {
        val ENCODING: String = "encoding"
        val CSV: String = "csv"
        val DATE: String = "date"

        private val logger = KotlinLogging.logger {}
        private val domain: String = "https://www.abcbourse.com"
        private val pathLibelles: String = "/download/historiques"
        private var token: String? = null
        private var encoding: Charset? = null
        private var csv: ByteArray? = null
    }

    override fun execute(contribution: StepContribution, chunkContext: ChunkContext): RepeatStatus {
        return runBlocking {
            requeteAbcBourse(contribution.stepExecution.jobExecution.executionContext)
        }
    }

    private suspend fun requeteAbcBourse(executionContext: ExecutionContext): RepeatStatus {
        val client = HttpClient(CIO) {
            install(HttpCookies)
        }
        token = serviceAbcBourse!!.getToken(client, domain + pathLibelles)
        logger.info { "RequestVerificationToken = $token" }
        val date: LocalDate = LocalDate.now()
        getCotations(client, date)
        logger.info { "Cotations ($encoding)${System.lineSeparator()} ${ByteArrayResource(csv!!).getContentAsString(encoding!!)}" }
        client.close()
        executionContext.putString(ENCODING, encoding!!.name())
        executionContext.put(CSV, csv)
        executionContext.put(DATE, date)
        return RepeatStatus.FINISHED
    }

    private suspend fun getCotations(client: HttpClient, date: LocalDate) {
        val response: HttpResponse = submitFormLibelles(client, date)
        if (response.status.value == 200) { // TODO : check date in filename = date
            encoding = serviceAbcBourse!!.findEncoding(response)
            val bytes: ByteArray = response.body()
            csv = GZIPInputStream(bytes.inputStream())
                .readAllBytes()
        } else {
            throw UnexpectedJobExecutionException(response.toString())
        }
    }

    private suspend fun submitFormLibelles(client: HttpClient, date: LocalDate): HttpResponse {
        // https://ktor.io/docs/client-requests.html#form_parameters
        // https://ktor.io/docs/client-responses.html#streaming
        val strDate: String = date.format(DateTimeFormatter.ISO_DATE)
        val response: HttpResponse = client.submitForm(
            url = domain + pathLibelles,
            formParameters = parameters {
                append("dateFrom", strDate)
                append("__Invariant", "dateFrom")
                append("dateTo", strDate)
                append("__Invariant", "dateTo")
                append("cbox", "eurolistap")
                append("cbox", "eurolistbp")
                append("cbox", "eurolistcp")
                append("txtOneSico", "")
                append("sFormat", "ab")
                append("typeData", "ticker")
                append("cbYes", "false")
                append("__RequestVerificationToken", token!!)
            }
        ) {
            headers {
                append(
                    HttpHeaders.Accept,
                    "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7"
                )
                append(HttpHeaders.AcceptEncoding, "gzip, deflate, br, zstd")
                append(HttpHeaders.AcceptLanguage, "fr-FR,fr;q=0.9,en-US;q=0.8,en;q=0.7")
                append(HttpHeaders.CacheControl, "max-age=0")
                append(HttpHeaders.Origin, domain)
                append(HttpHeaders.Referrer, domain + pathLibelles)
            }
        }
        return response
    }
}