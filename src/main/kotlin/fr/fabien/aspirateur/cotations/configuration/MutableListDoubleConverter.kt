package fr.fabien.aspirateur.cotations.configuration

import jakarta.persistence.AttributeConverter

class MutableListDoubleConverter : AttributeConverter<MutableList<Double>, String> {
    override fun convertToDatabaseColumn(listDouble: MutableList<Double>): String {
        return "[${listDouble.joinToString(",")}]"
    }

    override fun convertToEntityAttribute(strDoubles: String): MutableList<Double> {
        return strDoubles
            .substring(1, strDoubles.length - 1)
            .split(",")
            .filter { strDouble -> strDouble.isNotEmpty() }
            .map { strDouble -> strDouble.toDouble() }
            .toMutableList()
    }
}