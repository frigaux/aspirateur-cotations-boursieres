package fr.fabien.aspirateur.cotations.repository

import fr.fabien.aspirateur.cotations.entity.Libelle
import org.springframework.data.repository.CrudRepository

interface LibelleRepository : CrudRepository<Libelle, Int>