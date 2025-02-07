package fr.fabien.aspirateur.cotations.entity

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
    var ouverture: Float,

    @Column(nullable = false)
    var plusHaut: Float,

    @Column(nullable = false)
    var plusBas: Float,

    @Column(nullable = false)
    var cloture: Float,

    @Column(nullable = false)
    var volume: Long,

    @Lob
    @Column(nullable = true, length = 65535)
    var moyennesMobiles: String?,// TODO : marchall / unmarchal la liste des MM

    @Column(nullable = true)
    var alerte: Boolean?,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null
)