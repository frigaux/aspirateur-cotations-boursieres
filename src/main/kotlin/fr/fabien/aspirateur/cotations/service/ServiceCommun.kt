package fr.fabien.aspirateur.cotations.service

import io.ktor.client.statement.HttpResponse
import org.springframework.batch.core.UnexpectedJobExecutionException
import org.springframework.stereotype.Service

@Service
class ServiceCommun {
    public suspend fun findFilename(response: HttpResponse, regexpFilename: String): String {
        val contentDisposition: String? = response.headers.get("content-disposition")
        return contentDisposition?.let {
            regexpFilename
                .toRegex()
                .find(it)
                ?.let {
                    it.groups[1]!!.value
                }
                ?: run {
                    throw UnexpectedJobExecutionException("Filename not found in contentDisposition : $contentDisposition")
                }
        }
            ?: run {
                throw UnexpectedJobExecutionException("content-disposition not found in response headers : ${response.headers}")
            }
    }
}