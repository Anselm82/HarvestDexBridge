package com.honeywell.usbakerydex.honeywelldex.model

enum class TestIndicator(val value: String) {
    TEST("T"),
    PRODUCTION("P");

    companion object {
        fun fromValue(value: String): TestIndicator {
            return values().firstOrNull() { it.value === value } ?: PRODUCTION
        }
    }
}