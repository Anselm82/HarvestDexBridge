package com.honeywell.usbakerydex.dex.model.vo

class ReferenceIdentificationQualifier {
    companion object {
        const val CARRIER_ASSIGNED_PACKAGE_IDENTIFICATION_NUMBER = "08" // To provide 3r party delivery service tracking number
        const val PROVINCIAL_TAX_IDENTIFICATION = "4G"
        const val CANADIAN_GOODS_AND_SERVICES_OR_QUEBEC_SALES_TAX_REFERENCE_NUMBER = "4O"
        const val SHIPPING_LABEL_SERIAL_NUMBER = "LA" // GS1 EAUN UCC SSCC

        fun values() : List<String> {
            return arrayListOf(CARRIER_ASSIGNED_PACKAGE_IDENTIFICATION_NUMBER, PROVINCIAL_TAX_IDENTIFICATION, CANADIAN_GOODS_AND_SERVICES_OR_QUEBEC_SALES_TAX_REFERENCE_NUMBER, SHIPPING_LABEL_SERIAL_NUMBER)
        }
    }
}