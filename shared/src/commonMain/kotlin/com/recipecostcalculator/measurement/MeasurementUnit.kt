package com.recipecostcalculator.measurement

enum class MeasurementUnit(
    val dimension: MeasurementDimension,
    val toCanonicalFactor: Double,
    val label: String,
) {
    KG(MeasurementDimension.MASS, 1.0, "kg"),
    G(MeasurementDimension.MASS, 0.001, "g"),
    L(MeasurementDimension.VOLUME, 1.0, "lt"),
    ML(MeasurementDimension.VOLUME, 0.001, "ml"),
    CC(MeasurementDimension.VOLUME, 0.001, "cc"),
    UN(MeasurementDimension.COUNT, 1.0, "un");

    companion object {
        fun canonicalFor(dimension: MeasurementDimension): MeasurementUnit = when (dimension) {
            MeasurementDimension.MASS -> KG
            MeasurementDimension.VOLUME -> L
            MeasurementDimension.COUNT -> UN
        }

        fun unitsFor(dimension: MeasurementDimension): List<MeasurementUnit> =
            entries.filter { it.dimension == dimension }

        fun fromLabel(label: String): MeasurementUnit =
            entries.firstOrNull { it.label == label }
                ?: entries.firstOrNull { it.name.equals(label, ignoreCase = true) }
                ?: KG
    }
}
