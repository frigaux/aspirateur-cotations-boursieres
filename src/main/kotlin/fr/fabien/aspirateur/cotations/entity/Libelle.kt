package fr.fabien.aspirateur.cotations.entity

import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(
    indexes = arrayOf(Index(columnList = "date")),
    uniqueConstraints = arrayOf(
        UniqueConstraint(name = "UniqueDateAndTicker", columnNames = arrayOf("date", "ticker"))
    )
)
class Libelle(
    @Temporal(TemporalType.DATE)
    @Column(nullable = false, updatable = false)
    val date: LocalDate,

    @Column(nullable = false, length = 13)
    var isin: String,

    @Column(nullable = false, updatable = false, length = 5)
    val ticker: String,

    @Column(nullable = false, length = 100)
    var nom: String,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null
)