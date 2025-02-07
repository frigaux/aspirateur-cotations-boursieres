package fr.fabien.aspirateur.cotations.repository

import fr.fabien.aspirateur.cotations.entity.abcbourse.AbcCotation
import org.springframework.data.repository.CrudRepository

interface RepositoryAbcCotation : CrudRepository<AbcCotation, Int> {
}