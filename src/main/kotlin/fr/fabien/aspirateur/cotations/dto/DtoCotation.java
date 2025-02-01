package fr.fabien.aspirateur.cotations.dto;

import java.time.LocalDate;

// FlatFileItemReader require a java record, not a kotlin data class !!
public record DtoCotation(LocalDate date, String ticker, Float ouverture, Float plusHaut, Float plusBas, Float cloture,
                          Long volume) {
}
