package fr.fabien.aspirateur.cotations.repository

import fr.fabien.aspirateur.cotations.entity.Libelle
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import java.time.LocalDate

interface RepositoryLibelle : CrudRepository<Libelle, Int> {
    fun findByDateAndTickerIn(date: LocalDate, tickers: List<String>): List<Libelle>

    @Query("SELECT l FROM Libelle l JOIN FETCH l.cotation WHERE l.date = :date AND l.ticker IN (:tickers)")
    fun queryByDateAndTickerIn(@Param("date") date: LocalDate, @Param("tickers") tickers: List<String>): List<Libelle>
}