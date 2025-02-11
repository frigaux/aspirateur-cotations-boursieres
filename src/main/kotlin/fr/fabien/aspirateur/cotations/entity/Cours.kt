package fr.fabien.aspirateur.cotations.entity

import fr.fabien.aspirateur.cotations.configuration.MutableListDoubleConverter
import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(
    indexes = [
        Index(columnList = "date")
    ],
    uniqueConstraints = [
        UniqueConstraint(name = "UniqueValeurEtDate", columnNames = ["idValeur", "date"])
    ]
)
class Cours(
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idValeur", nullable = false, updatable = false)
    val valeur: Valeur,

    @Temporal(TemporalType.DATE)
    @Column(nullable = false, updatable = false)
    val date: LocalDate,

    @Column(nullable = false)
    var ouverture: Double,

    @Column(nullable = false)
    var plusHaut: Double,

    @Column(nullable = false)
    var plusBas: Double,

    @Column(nullable = false)
    var cloture: Double,

    @Column(nullable = false)
    var volume: Long,

    @Lob
    @Column(nullable = false, length = 65535)
    @Convert(converter = MutableListDoubleConverter::class)
    val moyennesMobiles: MutableList<Double>,

    @Column(nullable = false)
    var alerte: Boolean,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null
)