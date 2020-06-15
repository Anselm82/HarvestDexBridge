package com.honeywell.usbakerydex.dex.model.vo

class TestIndicator {
    companion object {
        const val TEST = "T"
        const val PRODUCTION = "P"

        fun values(): List<String> {
            return arrayListOf(
                TEST,
                PRODUCTION
            )
        }
    }
}