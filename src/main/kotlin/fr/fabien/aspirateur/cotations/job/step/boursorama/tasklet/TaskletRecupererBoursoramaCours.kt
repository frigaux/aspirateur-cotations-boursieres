package fr.fabien.aspirateur.cotations.job.step.boursorama.tasklet

import fr.fabien.aspirateur.cotations.ApplicationAspirateur
import fr.fabien.aspirateur.cotations.service.ServiceBoursorama
import fr.fabien.aspirateur.cotations.service.ServiceCommun
import fr.fabien.jpa.cotations.Marche
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.statement.*
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
import java.io.ByteArrayOutputStream
import java.io.OutputStreamWriter
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Component
@Scope("singleton")
class TaskletRecupererBoursoramaCours(
    val serviceCommun: ServiceCommun,
    val serviceBoursorama: ServiceBoursorama
) : Tasklet {

    companion object {
        // job execution context keys
        val CSV: String = "csv"

        private val logger = KotlinLogging.logger {}
    }

    override fun execute(contribution: StepContribution, chunkContext: ChunkContext): RepeatStatus {
        return runBlocking {
            val date: LocalDate = contribution.stepExecution.jobParameters.getLocalDate(ApplicationAspirateur.DATE)!!
            requetesBoursorama(contribution.stepExecution.jobExecution.executionContext, date)
        }
    }

    private suspend fun requetesBoursorama(executionContext: ExecutionContext, date: LocalDate): RepeatStatus {
        val client = HttpClient(CIO) {
            install(HttpCookies)
        }

        serviceBoursorama.authentifierEtCookies(client)

        val name: String = serviceBoursorama.getFormulaire(client)
        logger.info { "Nom du paramètre télécharger = $name" }

        val csv: ByteArray = telechargerCours(client, name, date, Marche.EURO_LIST_A) +
                telechargerCours(client, name, date, Marche.EURO_LIST_B) +
                telechargerCours(client, name, date, Marche.EURO_LIST_C)

        logger.info { "Fichier final :${System.lineSeparator()}${csv.toString(Charsets.UTF_8)}" }

        executionContext.put(CSV, csv)

        client.close()
        return RepeatStatus.FINISHED
    }

    private suspend fun telechargerCours(
        client: HttpClient,
        name: String,
        date: LocalDate,
        marche: Marche
    ): ByteArray {
        val lienTelechargement: String = serviceBoursorama.postFormulaire(client, name, date, marche)
        logger.info { "Lien pour le téléchargement = $lienTelechargement" }

        val response: HttpResponse = serviceBoursorama.telecharger(client, lienTelechargement);
        val filename = serviceCommun.findFilename(response, "filename=\"([^\"]+)\"")
        logger.info { "Nom du fichier téléchargé = $filename" }
        if (!filename.contains(date.format(DateTimeFormatter.ISO_DATE))) {
            throw UnexpectedJobExecutionException("Le nom du fichier $filename ne correspond pas à la date demandée $date")
        }
        val csv: ByteArray = response.bodyAsBytes()
        logger.info { "Contenu du fichier téléchargé pour le marché ${marche} :${System.lineSeparator()}${csv.toString(Charsets.UTF_8)}" }

        return ajouterMarche(csv, marche)
    }

    private fun ajouterMarche(csv: ByteArray, marche: Marche): ByteArray {
        val output = ByteArrayOutputStream()
        val writer = OutputStreamWriter(output, Charsets.UTF_8)
        ByteArrayResource(csv).inputStream
            .reader(Charsets.UTF_8)
            .readLines()
            .drop(1)
            .forEach { line ->
                writer.write("${line}\t${marche}${System.lineSeparator()}")
            };
        writer.flush();
        return output.toByteArray();
    }
}