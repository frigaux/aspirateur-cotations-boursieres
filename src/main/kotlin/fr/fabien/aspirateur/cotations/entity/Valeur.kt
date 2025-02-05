package fr.fabien.aspirateur.cotations.entity

import jakarta.persistence.*

@Entity
@Table(
    indexes = arrayOf(Index(columnList = "ticker"))
)
class Valeur(
    @Column(nullable = false, updatable = false, length = 5)
    val ticker: String,

    @Column(nullable = false, updatable = false, length = 30)
    val marche: String,

    @Column(nullable = false, length = 100)
    var libelle: String,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null
)