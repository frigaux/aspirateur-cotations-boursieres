package fr.fabien.aspirateur.cotations.repository

import fr.fabien.aspirateur.cotations.entity.Valeur
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import java.time.LocalDate

interface RepositoryValeur : CrudRepository<Valeur, Int> {
    @Query("SELECT l FROM Valeur l JOIN FETCH l.cours c WHERE c.date = :date")
    fun queryJoinCoursByDate(@Param("date") date: LocalDate): List<Valeur>
}