package fr.fabien.aspirateur.cotations.configuration

import jakarta.persistence.AttributeConverter

class ListFloatConverter : AttributeConverter<List<Float>, String> {
    override fun convertToDatabaseColumn(listFloat: List<Float>): String {
        return "[${listFloat.joinToString(",")}]"
    }

    override fun convertToEntityAttribute(strFloats: String): List<Float> {
        return strFloats
            .substring(1, strFloats.length - 1)
            .split(",")
            .filter { strFloat -> strFloat.isNotEmpty() }
            .map { strFloat -> strFloat.toFloat() }
    }
}