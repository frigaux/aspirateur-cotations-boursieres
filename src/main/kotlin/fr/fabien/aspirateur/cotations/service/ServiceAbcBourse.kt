package fr.fabien.aspirateur.cotations.service

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.springframework.batch.core.UnexpectedJobExecutionException
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.nio.charset.Charset

@Service
class ServiceAbcBourse(
    @Value("#{systemProperties['abc.login']}") private val login: String,
    @Value("#{systemProperties['abc.password']}") private val password: String
) {
    companion object {
        private val URL_DOMAINE: String = "https://www.abcbourse.com"
        private val CHEMIN_FORMULAIRE_AUTHENTIFICATION: String = "/help2/login11.html"
        private val CHEMIN_AUTHENTIFICATION = "/api/general/loginUser"
    }

    suspend fun authentifierEtCookies(client: HttpClient, referrer: String): List<Cookie> {
        val responseGET: HttpResponse = client.get("${URL_DOMAINE}${CHEMIN_FORMULAIRE_AUTHENTIFICATION}")
        if (responseGET.status == HttpStatusCode.OK) {
            val responsePOST: HttpResponse = client.post("${URL_DOMAINE}${CHEMIN_AUTHENTIFICATION}") {
                contentType(ContentType.Application.Json)
                setBody(Authentification(login, password))
                headers {
                    append(HttpHeaders.Accept, "*/*")
                    append(HttpHeaders.AcceptEncoding, "gzip, deflate, br, zstd")
                    append(HttpHeaders.AcceptLanguage, "fr-FR,fr;q=0.9,en-US;q=0.8,en;q=0.7")
                    append(HttpHeaders.CacheControl, "max-age=0")
                    append(HttpHeaders.Origin, URL_DOMAINE)
                    append(HttpHeaders.Referrer, referrer)
                }
            }
            if (responsePOST.status != HttpStatusCode.OK) {
                throw UnexpectedJobExecutionException("L'authentification a échouée : ${responsePOST.toString()}")
            }
            return responsePOST.setCookie()
        } else {
            throw UnexpectedJobExecutionException("Impossible de récupérer le formulaire d'authentification : ${responseGET.toString()}")
        }
    }

    public suspend fun formulaireTelechargement(client: HttpClient, urlString: String): String {
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