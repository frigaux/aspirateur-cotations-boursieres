package fr.fabien.aspirateur.cotations.entity

import jakarta.persistence.*
import java.time.LocalDate

@Entity
class Libelle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private var id: Int? = null

    @Temporal(TemporalType.DATE)
    @Column(nullable = false)
    private var date: LocalDate? = null

    @Column(name = "ISIN", unique = true, nullable = false, updatable = false, length = 13)
    private var isin: String? = null

    @Column(unique = true, nullable = false, updatable = false, length = 5)
    private var ticker: String? = null

    @Column(unique = true, nullable = false, updatable = false, length = 100)
    private var nom: String? = null
}