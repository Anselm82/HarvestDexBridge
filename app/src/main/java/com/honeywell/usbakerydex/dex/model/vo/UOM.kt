package com.honeywell.usbakerydex.dex.model.vo

class UOM {

    companion object {
        const val BOX = "BX"
        const val CASE = "CA"
        const val CARTON = "CT"
        const val DOZEN = "DZ"
        const val EACH = "EA"
        const val GALLON = "GA"
        const val KEG = "KE"
        const val KILOGRAM = "KG"
        const val POUND = "LB"
        const val PACKAGE = "PK"
        const val PALLET = "PL"
        const val TANK = "TK"
        const val UNIT = "UN"

        fun values(): List<String> {
            return arrayListOf(
                BOX,
                CARTON,
                DOZEN,
                GALLON,
                KEG,
                KILOGRAM,
                POUND,
                PACKAGE,
                PALLET,
                TANK,
                UNIT,
                EACH,
                CASE
            )
        }

        fun supportedValues(): List<String> {
            return arrayListOf(EACH)
        }
    }
}