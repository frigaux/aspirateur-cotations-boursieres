package fr.fabien.aspirateur.cotations.configuration.step.tasklet

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
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.context.annotation.Scope
import org.springframework.core.io.ByteArrayResource
import org.springframework.stereotype.Component
import java.nio.charset.Charset
import java.util.zip.GZIPInputStream

@Component
@Scope("singleton")
class TaskletRecupererLibelles : Tasklet {
    companion object {
        private val logger = KotlinLogging.logger {}
        private val domain: String = "https://www.abcbourse.com"
        private val pathLibelles: String = "/download/libelles"
        var token: String? = null
        var encoding: Charset? = null
        var csv: ByteArrayResource? = null
    }

    override fun execute(contribution: StepContribution, chunkContext: ChunkContext): RepeatStatus {
        return runBlocking {
            requeteAbcBourse()
        }
    }

    private suspend fun requeteAbcBourse(): RepeatStatus {
        val client = HttpClient(CIO) {
            install(HttpCookies)
        }
        getToken(client)
        logger.info { "RequestVerificationToken = $token" }
        getLibelles(client)
        logger.info { "Libellés ($encoding)${System.lineSeparator()} ${csv!!.getContentAsString(encoding!!)}" }
        client.close()
        return RepeatStatus.FINISHED
    }

    private suspend fun getToken(client: HttpClient) {
        val response: HttpResponse = client.get(domain + pathLibelles)
        if (response.status.value == 200) {
            val regexpToken = "\"__RequestVerificationToken\" type=\"hidden\" value=\"([^\"]+)\""
            val content = response.bodyAsText()
            token = regexpToken
                .toRegex()
                .find(content)
                ?.let {
                    it.groups[1]!!.value
                }
                ?: run {
                    throw UnexpectedJobExecutionException("RequestVerificationToken introuvable dans $content")
                }
        } else {
            throw UnexpectedJobExecutionException(response.toString())
        }
    }

    private suspend fun getLibelles(client: HttpClient) {
        val response: HttpResponse = submitFormLibelles(client)
        if (response.status.value == 200) {
            encoding = findEncoding(response)
            val bytes: ByteArray = response.body()
            csv = ByteArrayResource(
                GZIPInputStream(bytes.inputStream())
                    .readAllBytes()
            )
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
                append("cbox", "xcac40p")
                append("cbPlace", "true")
                append("__RequestVerificationToken", token!!)
                append("cbPlace", "false")
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

    private suspend fun findEncoding(response: HttpResponse): Charset {
        val regexpToken = "filename\\*=(.*)''"
        val contentDisposition : String? = response.headers.get("content-disposition")
        return contentDisposition?.let {
            regexpToken
                .toRegex()
                .find(it)
                ?.let {
                    Charset.forName(it.groups[1]!!.value)
                }
                ?: run {
                    throw UnexpectedJobExecutionException("Encoding not found in contentDisposition : $contentDisposition")
                }
        }
            ?: run {
                throw UnexpectedJobExecutionException("content-disposition not found in response headers : ${response.headers}")
            }
    }
}