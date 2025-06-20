package fr.fabien.aspirateur.cotations.dto.boursorama;

import fr.fabien.jpa.cotations.Marche;

import java.time.LocalDate;

// FlatFileItemReader require a java record, not a kotlin data class !!
public record DtoBoursoramaCours(Marche marche, String ticker, String nom, LocalDate date, Double ouverture,
                                 Double plusHaut, Double plusBas, Double cloture,
                                 Long volume, String devise) {
}