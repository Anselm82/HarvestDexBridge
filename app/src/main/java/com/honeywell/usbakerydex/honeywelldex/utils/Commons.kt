package com.honeywell.usbakerydex.honeywelldex.utils

enum class HoneywellInitiatorCode(val value: String) {
    SUPPLIER("S"),
    RETAILER("R")
}

enum class HoneywellHandlingMethodCodes(val value: String) {
    OFF_INVOICE("02"),
    INFORMATION_ONLY("15"),
    NOT_PROCESSED("12");

    companion object {
        fun fromValue(value: String): HoneywellHandlingMethodCodes {
            return values().first { item -> item.value.equals(value, true) }
        }
    }
}


enum class HoneywellUnitOfMeasure(val value: String) {
    BOX("BX"),
    CASE("CA"),
    CARTON("CT"),
    DOZEN("DZ"),
    EACH("EA"),
    GALLON("GA"),
    KEG("KE"), //100 pounds,
    KILOGRAM("KG"),
    POUND("LB"),
    PACKAGE("PK"),
    PALLET("PL"),
    TANK("TK"),
    UNIT("UN");

    companion object {
        fun fromValue(value: String) : HoneywellUnitOfMeasure {
            return values().first { it.value == value }
        }
    }
}

enum class HoneywellReferenceNumberQualifier(val value: String) {
    QUEBEC_OR_CGS("4O"),
    PROVINCIAL_TIN("4G")
}

enum class HoneywellTypeFlag(val value: String) {
    DEBIT("D"),
    CREDIT("C");

    companion object {
        fun fromValue(value: String): HoneywellTypeFlag {
            return values().first { item -> item.value.equals(value, true) }
        }
    }
}