package fr.fabien.aspirateur.cotations.job.step.abcbourse.tasklet

import fr.fabien.aspirateur.cotations.ApplicationAspirateur
import fr.fabien.aspirateur.cotations.service.ServiceAbcBourse
import fr.fabien.aspirateur.cotations.service.ServiceCommun
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
import org.springframework.context.annotation.Scope
import org.springframework.core.io.ByteArrayResource
import org.springframework.stereotype.Component
import java.nio.charset.Charset
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.zip.GZIPInputStream

// TODO : il y a désormais besoin d'une authentification
@Component
@Scope("singleton")
class TaskletRecupererAbcCotations(
    val serviceCommun: ServiceCommun,
    val serviceAbcBourse: ServiceAbcBourse
) : Tasklet {

    companion object {
        // job execution context keys
        val CHARSET: String = "charset"
        val CSV: String = "csv"

        private val logger = KotlinLogging.logger {}
        private val domain: String = "https://www.abcbourse.com"
        private val pathLibelles: String = "/download/historiques"
        private var token: String? = null
        private var charset: Charset? = null
        private var csv: ByteArray? = null
    }

    override fun execute(contribution: StepContribution, chunkContext: ChunkContext): RepeatStatus {
        return runBlocking {
            val date: LocalDate = contribution.stepExecution.jobParameters.getLocalDate(ApplicationAspirateur.DATE)!!
            requetesAbcBourse(contribution.stepExecution.jobExecution.executionContext, date)
        }
    }

    private suspend fun requetesAbcBourse(executionContext: ExecutionContext, date: LocalDate): RepeatStatus {
        val client = HttpClient(CIO) {
            install(HttpCookies)
        }
        token = serviceAbcBourse.getToken(client, domain + pathLibelles)
        logger.info { "RequestVerificationToken = $token" }
        getCotations(client, date)
        logger.info {
            "Cotations ($charset)${System.lineSeparator()}${
                ByteArrayResource(csv!!).getContentAsString(
                    charset!!
                )
            }"
        }
        client.close()
        executionContext.putString(CHARSET, charset!!.name())
        executionContext.put(CSV, csv)
        return RepeatStatus.FINISHED
    }

    private suspend fun getCotations(client: HttpClient, date: LocalDate) {
        val response: HttpResponse = submitFormLibelles(client, date)
        if (response.status.value == 200) {
            charset = serviceAbcBourse.findCharset(response)
            serviceAbcBourse.findError(response, charset!!)?.let{
                throw UnexpectedJobExecutionException(it)
            }
            val filename = serviceCommun.findFilename(response, "filename=([^;]+);")
            if (!filename.contains(date.format(DateTimeFormatter.BASIC_ISO_DATE))) {
                throw UnexpectedJobExecutionException("Le nom du fichier $filename ne correspond pas à la date demandée $date")
            }
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
        return client.submitForm(
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
    }
}