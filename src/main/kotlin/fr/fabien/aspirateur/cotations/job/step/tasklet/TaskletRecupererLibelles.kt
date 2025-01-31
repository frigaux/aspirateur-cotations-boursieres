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
import java.util.zip.GZIPInputStream

@Component
@Scope("singleton")
class TaskletRecupererLibelles : Tasklet {
    @Autowired
    val serviceAbcBourse: ServiceAbcBourse? = null

    companion object {
        val CHARSET: String = "charset"
        val CSV: String = "csv"
        val DATE: String = "date"

        private val logger = KotlinLogging.logger {}
        private val domain: String = "https://www.abcbourse.com"
        private val pathLibelles: String = "/download/libelles"
        private var token: String? = null
        private var charset: Charset? = null
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
        getLibelles(client)
        logger.info { "Libellés ($charset)${System.lineSeparator()} ${ByteArrayResource(csv!!).getContentAsString(charset!!)}" }
        client.close()
        executionContext.putString(CHARSET, charset!!.name())
        executionContext.put(CSV, csv)
        executionContext.put(DATE, LocalDate.now())
        return RepeatStatus.FINISHED
    }

    private suspend fun getLibelles(client: HttpClient) {
        val response: HttpResponse = submitFormLibelles(client)
        if (response.status.value == 200) {
            charset = serviceAbcBourse!!.findCharset(response)
            val bytes: ByteArray = response.body()
            csv = GZIPInputStream(bytes.inputStream())
                .readAllBytes()
        } else {
            throw UnexpectedJobExecutionException(response.toString())
        }
    }

    private suspend fun submitFormLibelles(client: HttpClient): HttpResponse {
        // https://ktor.io/docs/client-requests.html#form_parameters
        // https://ktor.io/docs/client-responses.html#streaming
        val response: HttpResponse = client.submitForm(
            url = domain + pathLibelles,
            formParameters = parameters {
//                append("cbox", "xcac40p")
                append("cbox", "eurolistap")
                append("cbox", "eurolistbp")
                append("cbox", "eurolistcp")
                append("cbPlace", "true")
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