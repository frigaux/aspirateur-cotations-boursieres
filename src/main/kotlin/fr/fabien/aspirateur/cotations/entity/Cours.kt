package fr.fabien.aspirateur.cotations.entity

import jakarta.persistence.*
import java.time.LocalDate

@Entity
class Cours(
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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null
)