package fr.fabien.aspirateur.cotations.repository

import fr.fabien.aspirateur.cotations.entity.Cotation
import org.springframework.data.repository.CrudRepository

interface RepositoryCotation : CrudRepository<Cotation, Int> {
}