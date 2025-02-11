package fr.fabien.aspirateur.cotations.entity.abcbourse

import jakarta.persistence.*

@Entity
class AbcCotation(
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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null
)