package fr.fabien.aspirateur.cotations.service

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.HttpStatusCode
import org.springframework.batch.core.UnexpectedJobExecutionException
import org.springframework.stereotype.Service
import java.nio.charset.Charset

@Service
class ServiceAbcBourse {
    public suspend fun getToken(client: HttpClient, urlString: String): String {
        val response: HttpResponse = client.get(urlString)
        if (response.status == HttpStatusCode.OK) {
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

    public suspend fun findCharset(response: HttpResponse): Charset {
        val regexpContentType = "charset=(.+)"
        val contentType: String? = response.headers.get("content-type")
        return contentType?.let {
            regexpContentType
                .toRegex()
                .find(it)
                ?.let {
                    Charset.forName(it.groups[1]!!.value)
                }
                ?: run {
                    val regexpContentDisposition = "filename\\*=(.+)''"
                    val contentDisposition: String? = response.headers.get("content-disposition")
                    return contentDisposition?.let {
                        regexpContentDisposition
                            .toRegex()
                            .find(it)
                            ?.let {
                                Charset.forName(it.groups[1]!!.value)
                            }
                            ?: run {
                                throw UnexpectedJobExecutionException("Charset not found in contentDisposition : $contentDisposition")
                            }
                    }
                        ?: run {
                            throw UnexpectedJobExecutionException("content-disposition not found in response headers : ${response.headers}")
                        }
                }
        }
            ?: run {
                throw UnexpectedJobExecutionException("content-type not found in response headers : ${response.headers}")
            }
    }

    public suspend fun findError(response: HttpResponse, charset: Charset): String? {
        val regexpError = " id=\"lblerror\">([^<]+)</p>"
        return regexpError
            .toRegex()
            .find(response.bodyAsText(charset))
            ?.let {
                it.groups[1]!!.value
            }
    }
}