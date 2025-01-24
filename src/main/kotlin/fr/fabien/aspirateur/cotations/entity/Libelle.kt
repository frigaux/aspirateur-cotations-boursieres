package fr.fabien.aspirateur.cotations.entity

import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(
    indexes = arrayOf(Index(columnList = "date")),
    uniqueConstraints = arrayOf(
        UniqueConstraint(name = "UniqueDateAndIsin", columnNames = arrayOf("date", "isin")),
        UniqueConstraint(name = "UniqueDateAndTicker", columnNames = arrayOf("date", "ticker")),
        UniqueConstraint(name = "UniqueDateAndNom", columnNames = arrayOf("date", "nom"))
    )
)
class Libelle(
    @Temporal(TemporalType.DATE)
    @Column(nullable = false, updatable = false)
    val date: LocalDate,

    @Column(nullable = false, updatable = false, length = 13)
    val isin: String,

    @Column(nullable = false, length = 5)
    val ticker: String,

    @Column(nullable = false, length = 100)
    val nom: String,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null
)