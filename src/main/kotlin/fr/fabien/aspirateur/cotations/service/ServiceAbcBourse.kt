package fr.fabien.aspirateur.cotations.service

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import org.springframework.batch.core.UnexpectedJobExecutionException
import org.springframework.stereotype.Service
import java.nio.charset.Charset

@Service
class ServiceAbcBourse {
    public suspend fun getToken(client: HttpClient, urlString: String): String {
        val response: HttpResponse = client.get(urlString)
        if (response.status.value == 200) {
            val regexpToken = "\"__RequestVerificationToken\" type=\"hidden\" value=\"([^\"]+)\""
            val content = response.bodyAsText()
            return regexpToken
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

    public suspend fun findEncoding(response: HttpResponse): Charset {
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