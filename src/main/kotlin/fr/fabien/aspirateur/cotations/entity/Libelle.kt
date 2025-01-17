package fr.fabien.aspirateur.cotations.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity
class Libelle {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private var id: Int? = null


}