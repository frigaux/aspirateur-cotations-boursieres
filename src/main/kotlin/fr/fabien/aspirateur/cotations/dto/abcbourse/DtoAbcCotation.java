package fr.fabien.aspirateur.cotations.dto.abcbourse;

import java.time.LocalDate;

// FlatFileItemReader require a java record, not a kotlin data class !!
public record DtoAbcCotation(String ticker, LocalDate date, Float ouverture,
                             Float plusHaut, Float plusBas, Float cloture,
                             Long volume) {
}
