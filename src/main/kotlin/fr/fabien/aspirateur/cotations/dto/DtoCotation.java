package fr.fabien.aspirateur.cotations.dto;

import java.time.LocalDate;

// FlatFileItemReader require a java record, not a kotlin data class !!
public record DtoCotation(String ticker, LocalDate date, Float ouverture,
                          Float plusHaut, Float plusBas, Float cloture,
                          Long volume) {
}
