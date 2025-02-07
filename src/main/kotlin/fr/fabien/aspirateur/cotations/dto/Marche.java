package fr.fabien.aspirateur.cotations.dto;

public enum Marche {
    EURO_LIST_A("Eurolist A"),
    EURO_LIST_B("Eurolist B"),
    EURO_LIST_C("Eurolist C");

    public final String libelle;

    Marche(String libelle) {
        this.libelle = libelle;
    }
}
