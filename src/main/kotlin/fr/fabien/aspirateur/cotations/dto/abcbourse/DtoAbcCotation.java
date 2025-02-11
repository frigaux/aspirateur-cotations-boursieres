package fr.fabien.aspirateur.cotations.dto.abcbourse;

import java.time.LocalDate;

// FlatFileItemReader require a java record, not a kotlin data class !!
public record DtoAbcCotation(String ticker, LocalDate date, Double ouverture,
                             Double plusHaut, Double plusBas, Double cloture,
                             Long volume) {
}
