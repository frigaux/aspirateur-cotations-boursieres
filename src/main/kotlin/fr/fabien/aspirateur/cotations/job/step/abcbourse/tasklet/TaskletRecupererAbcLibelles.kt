package fr.fabien.aspirateur.cotations.job.step.abcbourse.tasklet

import fr.fabien.aspirateur.cotations.service.ServiceAbcBourse
import fr.fabien.jpa.cotations.Marche
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
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
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.util.zip.GZIPInputStream

@Component
@Scope("singleton")
class TaskletRecupererAbcLibelles(val serviceAbcBourse: ServiceAbcBourse) : Tasklet {

    companion object {
        // job execution context keys
        val CHARSET: String = "charset"
        val CSV: String = "csv"

        private val logger = KotlinLogging.logger {}
        private val domain: String = "https://www.abcbourse.com"
        private val pathLibelles: String = "/download/libelles"
        private var token: String? = null
        private var charset: Charset? = null
        private var csv: ByteArray? = null
    }

    override fun execute(contribution: StepContribution, chunkContext: ChunkContext): RepeatStatus {
        return runBlocking {
            requetesAbcBourse(contribution.stepExecution.jobExecution.executionContext)
        }
    }

    private suspend fun requetesAbcBourse(executionContext: ExecutionContext): RepeatStatus {
        val client = HttpClient(CIO) {
            install(HttpCookies)
            install(ContentNegotiation) {
                json()
            }
        }

        val cookies: List<Cookie> = serviceAbcBourse.authentifierEtCookies(client, domain + pathLibelles)
        logger.info { "Authentification réussie :${System.lineSeparator()}${cookies}" }

        token = serviceAbcBourse.formulaireTelechargement(client, domain + pathLibelles)
        logger.info { "RequestVerificationToken = $token" }

        telechargerLibelles(client)
        logger.info {
            "Libellés ($charset)${System.lineSeparator()}${
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

    private suspend fun telechargerLibelles(client: HttpClient) {
        val sb: StringBuilder = StringBuilder()
        telechargerLibelles(client, "eurolistap")
            .let { lines -> addlines(lines, sb, Marche.EURO_LIST_A) }
        telechargerLibelles(client, "eurolistbp")
            .let { lines -> addlines(lines, sb, Marche.EURO_LIST_B) }
        telechargerLibelles(client, "eurolistcp")
            .let { lines -> addlines(lines, sb, Marche.EURO_LIST_C) }
        csv = sb.toString().toByteArray(charset!!)
    }

    private fun addlines(lines: List<String>, sb: StringBuilder, marche: Marche) {
        for (i in 1..lines.size - 1) {
            sb.append("${lines[i]};$marche${System.lineSeparator()}")
        }
    }

    private suspend fun telechargerLibelles(client: HttpClient, cbox: String): List<String> {
        val response: HttpResponse = submitFormLibelles(client, cbox)
        if (response.status.value == 200 && response.headers.get("content-encoding") == "gzip") {
            charset = serviceAbcBourse.findCharset(response)
            return response.bodyAsBytes().inputStream()
                .let { inputStream -> InputStreamReader(GZIPInputStream(inputStream), charset!!) }
                .let { inputStreamReader -> BufferedReader(inputStreamReader) }
                .readLines()
        } else {
            throw UnexpectedJobExecutionException(response.toString())
        }
    }

    private suspend fun submitFormLibelles(client: HttpClient, cbox: String): HttpResponse {
        // https://ktor.io/docs/client-requests.html#form_parameters
        // https://ktor.io/docs/client-responses.html#streaming
        val response: HttpResponse = client.submitForm(
            url = domain + pathLibelles,
            formParameters = parameters {
                append("cbox", cbox)
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