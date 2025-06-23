package fr.fabien.aspirateur.cotations.service

import kotlinx.serialization.Serializable

@Serializable
data class Authentification(val pseudo: String, val pass: String) {
    val staylog: Boolean = false
}
