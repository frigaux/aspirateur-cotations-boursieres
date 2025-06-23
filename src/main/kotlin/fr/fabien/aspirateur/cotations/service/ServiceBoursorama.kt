package fr.fabien.aspirateur.cotations.service

import fr.fabien.jpa.cotations.Marche
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.springframework.batch.core.UnexpectedJobExecutionException
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Service
class ServiceBoursorama(
    @Value("#{systemProperties['boursorama.login']}") private val login: String,
    @Value("#{systemProperties['boursorama.password']}") private val password: String
) {
    companion object {
        private val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        private val URL_DOMAINE: String = "https://www.boursorama.com"
        private val CHEMIN_AUTHENTIFICATION: String = "/connexion/?org=/espace-membres/telecharger-cours/paris"
        private val CHEMIN_FORMULAIRE_EXPORT = "/espace-membres/telecharger-cours/paris"
    }

    public suspend fun authentifierEtCookies(client: HttpClient): List<Cookie> {
        val response: HttpResponse = client.submitForm(
            url = "${URL_DOMAINE}${CHEMIN_AUTHENTIFICATION}",
            formParameters = parameters {
                append("login_member[login]", login)
                append("login_member[password]", password)
                append("login_member[remember]", "0")
                append("login_member[connect]", "")
            }
        ) {
            headers("${URL_DOMAINE}${CHEMIN_AUTHENTIFICATION}")
        }
        if (response.status != HttpStatusCode.Found) {
            throw UnexpectedJobExecutionException("L'authentification a échouée : ${response.toString()}")
        }
        return response.setCookie()
    }

    public suspend fun formulaireTelechargement(client: HttpClient): String {
        val response: HttpResponse = client.get("${URL_DOMAINE}${CHEMIN_FORMULAIRE_EXPORT}")
        if (response.status == HttpStatusCode.OK) {
            val regexpToken = "name=\"([^\"]+)\"\\s+value=\"Télécharger\""
            val content = response.bodyAsText()
            return regexpToken
                .toRegex()
                .find(content)
                ?.let {
                    it.groups[1]!!.value
                }
                ?: run {
                    throw UnexpectedJobExecutionException("NameTélécharger introuvable dans $content")
                }
        } else {
            throw UnexpectedJobExecutionException("La récupération du formulaire d'export a échouée : ${response.toString()}")
        }
    }

    public suspend fun postFormulaire(client: HttpClient, name: String, date: LocalDate, marche: Marche): String {
        val strDate: String = date.format(DATE_FORMATTER)
        val response: HttpResponse = client.submitForm(
            url = "${URL_DOMAINE}${CHEMIN_FORMULAIRE_EXPORT}",
            formParameters = parameters {
                append("quote_search[type]", "index")
                append("quote_search[index]", when (marche) {
                    Marche.EURO_LIST_A -> "2201"
                    Marche.EURO_LIST_B -> "2202"
                    Marche.EURO_LIST_C -> "2203"
                })
                append("quote_search[customIndexesList]", "")
                append("quote_search[label]", "1")
                append("quote_search[code]", "1")
                append("quote_search[date]", "1")
                append("quote_search[open]", "1")
                append("quote_search[high]", "1")
                append("quote_search[low]", "1")
                append("quote_search[volume]", "1")
                append("quote_search[close]", "1")
                append("quote_search[currency]", "1")
                append("quote_search[startDate]", strDate)
                append("quote_search[endDate]", strDate)
                append("quote_search[fileFormat]", "EXCEL")
                append("quote_search[decimalFormat]", "POINT")
                append("quote_search[method]", "mnemonic")
                append(name, "Télécharger")
            }
        ) {
            headers("${URL_DOMAINE}${CHEMIN_FORMULAIRE_EXPORT}")
        }
        if (response.status == HttpStatusCode.Found && response.headers.contains("location")) {
            return response.headers.get("location")!!
        }
        throw UnexpectedJobExecutionException("La soumission du formulaire d'export a échouée (302 attendue avec header location) : ${response.toString()}")
    }

    public suspend fun telecharger(client: HttpClient, lienExport: String): HttpResponse {
        val response: HttpResponse = client.get(lienExport)
        if (response.status == HttpStatusCode.OK) {
            return response;
        } else {
            throw UnexpectedJobExecutionException("L'export a échoué' : ${response.toString()}")
        }
    }

    private fun HttpRequestBuilder.headers(referrer: String) {
        headers {
            append(
                HttpHeaders.Accept,
                "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7"
            )
            append(HttpHeaders.AcceptEncoding, "gzip, deflate, br, zstd")
            append(HttpHeaders.AcceptLanguage, "fr-FR,fr;q=0.9,en-US;q=0.8,en;q=0.7")
            append(HttpHeaders.CacheControl, "max-age=0")
            append(HttpHeaders.Origin, URL_DOMAINE)
            append(HttpHeaders.Referrer, referrer)
        }
    }
}