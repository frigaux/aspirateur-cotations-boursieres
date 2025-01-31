package fr.fabien.aspirateur.cotations.dto;

// FlatFileItemReader require a java record, not a kotlin data class !!
public record DtoCotation(String date, String ticker, Float ouverture, Float plusHaut, Float plusBas, Float cloture,
                          Long volume) {
}
