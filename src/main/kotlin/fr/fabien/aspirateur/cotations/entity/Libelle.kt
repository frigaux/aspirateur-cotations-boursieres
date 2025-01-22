package fr.fabien.aspirateur.cotations.entity

import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(indexes = arrayOf(Index(columnList = "isin")))
class Libelle(
    @Temporal(TemporalType.DATE)
    @Column(nullable = false)
    val date: LocalDate,

    @Column(name = "ISIN", unique = true, nullable = false, updatable = false, length = 13)
    val isin: String,

    @Column(unique = true, nullable = false, updatable = false, length = 5)
    val ticker: String,

    @Column(unique = true, nullable = false, updatable = false, length = 100)
    val nom: String,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null
)