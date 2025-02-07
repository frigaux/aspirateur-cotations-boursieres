package fr.fabien.aspirateur.cotations.repository

import fr.fabien.aspirateur.cotations.entity.Cours
import org.springframework.data.repository.CrudRepository

interface RepositoryCours : CrudRepository<Cours, Int> {
}