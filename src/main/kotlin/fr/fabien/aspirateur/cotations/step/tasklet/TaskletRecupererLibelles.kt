package fr.fabien.aspirateur.cotations.configuration.step.tasklet

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.springframework.batch.core.StepContribution
import org.springframework.batch.core.UnexpectedJobExecutionException
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope("singleton")
class TaskletRecupererLibelles : Tasklet {
    val logger = KotlinLogging.logger {}

    override fun execute(contribution: StepContribution, chunkContext: ChunkContext): RepeatStatus {
        return requeteAbcBourse()
    }

    private fun requeteAbcBourse(): RepeatStatus {
        return runBlocking {
            val client = HttpClient(CIO)
            val response: HttpResponse = client.get("https://www.abcbourse.com/download/libelles")
            client.close()
            if (response.status.value == 200) {
                val regexpToken = "\"__RequestVerificationToken\" type=\"hidden\" value=\"([^\"]+)\""
                val content = response.bodyAsText()
                regexpToken
                    .toRegex()
                    .find(content)
                    ?.let {
                        val token = it.groups[1]!!.value
                        logger.info { "RequestVerificationToken = ${token}" }
                    }
                    ?:run {
                        logger.info { "RequestVerificationToken introuvable dans ${content}" }
                        throw UnexpectedJobExecutionException("RequestVerificationToken introuvable")
                    }
            } else {
                throw UnexpectedJobExecutionException(response.toString())
            }
            RepeatStatus.FINISHED
        }
    }
}