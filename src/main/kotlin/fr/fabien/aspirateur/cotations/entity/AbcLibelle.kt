package fr.fabien.aspirateur.cotations.entity

import fr.fabien.aspirateur.cotations.dto.Marche
import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(
    indexes = [
        Index(columnList = "date"),
    ],
    uniqueConstraints = [
        UniqueConstraint(name = "UniqueDateEtTicker", columnNames = ["date", "ticker"])
    ]
)
class AbcLibelle(
    @Temporal(TemporalType.DATE)
    @Column(nullable = false, updatable = false)
    val date: LocalDate,

    @Column(nullable = false, updatable = false, length = 5)
    val ticker: String,

    @Column(nullable = false, length = 13)
    var isin: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    val marche: Marche,

    @Column(nullable = false, length = 100)
    var nom: String,

    @OneToOne(optional = true, cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cotation")
    var abcCotation: AbcCotation? = null,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null
)