package fr.fabien.aspirateur.cotations.job.dto;

// FlatFileItemReader require a java record, not a kotlin data class !!
public record Libelle(String isin, String nom, String ticker) {
}
