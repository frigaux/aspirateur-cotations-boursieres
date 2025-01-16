package fr.fabien.aspirateur.cotations.configuration.step.writer

import fr.fabien.aspirateur.cotations.job.dto.Libelle
import org.springframework.batch.item.Chunk
import org.springframework.batch.item.ItemWriter
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope("singleton")
class WriterLibelle : ItemWriter<Libelle> {
    override fun write(libelles: Chunk<out Libelle>) {
        TODO("Not yet implemented")
    }
}