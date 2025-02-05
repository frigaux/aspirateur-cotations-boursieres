package fr.fabien.aspirateur.cotations.dto;

// FlatFileItemReader require a java record, not a kotlin data class !!
public record DtoAbcLibelle(String ticker, String isin, String marche, String nom) {
}
