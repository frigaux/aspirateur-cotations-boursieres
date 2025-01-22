package fr.fabien.aspirateur.cotations.dto;

// FlatFileItemReader require a java record, not a kotlin data class !!
public record DtoLibelle(String isin, String nom, String ticker) {
}
