package fr.fabien.aspirateur.cotations.configuration.step.writer

import org.springframework.batch.item.Chunk
import org.springframework.batch.item.ItemWriter

class WriterLibelle : ItemWriter<String> {
    override fun write(chunk: Chunk<out String>) {
        TODO("Not yet implemented")
    }
}