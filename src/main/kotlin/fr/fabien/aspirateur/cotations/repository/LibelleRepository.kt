package fr.fabien.aspirateur.cotations.repository

import fr.fabien.aspirateur.cotations.entity.Libelle
import org.springframework.data.repository.CrudRepository
import java.time.LocalDate

interface LibelleRepository : CrudRepository<Libelle, Int> {
    fun deleteByDate(date: LocalDate)
}