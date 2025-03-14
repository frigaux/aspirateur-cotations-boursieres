package fr.fabien.aspirateur.cotations.dto.abcbourse;

import fr.fabien.jpa.cotations.Marche;

// FlatFileItemReader require a java record, not a kotlin data class !!
public record DtoAbcLibelle(String ticker, String isin, Marche marche, String nom) {
}
