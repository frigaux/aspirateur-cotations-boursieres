package fr.fabien.aspirateur.cotations.dto;

import fr.fabien.aspirateur.cotations.entity.Marche;

// FlatFileItemReader require a java record, not a kotlin data class !!
public record DtoAbcLibelle(String ticker, String isin, Marche marche, String nom) {
}
