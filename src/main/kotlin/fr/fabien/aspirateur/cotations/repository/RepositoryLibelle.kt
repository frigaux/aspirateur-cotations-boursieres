package fr.fabien.aspirateur.cotations.repository

import fr.fabien.aspirateur.cotations.entity.Libelle
import org.springframework.data.repository.CrudRepository
import java.time.LocalDate

interface RepositoryLibelle : CrudRepository<Libelle, Int> {
    fun findByDateAndTickerIn(date: LocalDate, tickers: List<String>): List<Libelle>
}