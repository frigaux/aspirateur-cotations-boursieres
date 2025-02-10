package fr.fabien.aspirateur.cotations.repository

import fr.fabien.aspirateur.cotations.entity.Cours
import fr.fabien.aspirateur.cotations.entity.Valeur
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import java.time.LocalDate

interface RepositoryCours : CrudRepository<Cours, Int> {
    fun findByDate(date: LocalDate): List<Cours>

    @Query("SELECT c FROM Cours c WHERE c.valeur = :valeur AND c.date <= :date ORDER BY c.date DESC LIMIT 300")
    fun query300BeforeDate(@Param("valeur") valeur: Valeur, @Param("date") date: LocalDate): List<Cours>
}