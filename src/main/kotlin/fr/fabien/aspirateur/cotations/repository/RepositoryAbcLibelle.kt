package fr.fabien.aspirateur.cotations.repository

import fr.fabien.aspirateur.cotations.entity.abcbourse.AbcLibelle
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import java.time.LocalDate

interface RepositoryAbcLibelle : CrudRepository<AbcLibelle, Int> {
    fun findByDateAndTickerIn(date: LocalDate, tickers: List<String>): List<AbcLibelle>

    @Query("SELECT l FROM AbcLibelle l LEFT JOIN FETCH l.abcCotation WHERE l.date = :date AND l.ticker IN (:tickers)")
    fun queryByDateAndTickerIn(@Param("date") date: LocalDate, @Param("tickers") tickers: List<String>): List<AbcLibelle>

    @Query("SELECT l FROM AbcLibelle l LEFT JOIN FETCH l.abcCotation WHERE l.date = :date")
    fun queryByDate(@Param("date") date: LocalDate): List<AbcLibelle>
}