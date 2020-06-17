package com.honeywell.usbakerydex.dex.model.blocks

import com.honeywell.usbakerydex.dex.model.vo.ReferenceIdentificationQualifier

data class N9(
    var referenceIdentificationQualifier: String? = ReferenceIdentificationQualifier.SHIPPING_LABEL_SERIAL_NUMBER, //01 M-[2-3] by default LA
    var referenceIdentification: String? = null, //02 depends on referenceIdentificationQualifier [1-80]
    var freeFormDescription: String? = null, //03 depends [1-45]
    var date: String? = null, //04 YYYYMMDD O-[8-8]
    var time: String? = null, //05 hhmmssdd depends [4-8]
    var timeCode: String? = null //06 UTC timezone where + is P or - is O-[2-2]
) : ExtraInformation() {
    fun putString(key: String, value: String?) {
        when (key) {
            "01" -> this.referenceIdentificationQualifier =
                if (value.isNullOrBlank() || value !in ReferenceIdentificationQualifier.values()) ReferenceIdentificationQualifier.SHIPPING_LABEL_SERIAL_NUMBER else value
            "02" -> this.referenceIdentification = value
            "03" -> this.freeFormDescription = value
            "04" -> this.date = value
            "05" -> this.time = value
            "06" -> this.timeCode = value
        }
    }

    fun getString(key: String): String? {
        return when (key) {
            "01" -> this.referenceIdentificationQualifier
            "02" -> this.referenceIdentification
            "03" -> this.freeFormDescription
            "04" -> this.date
            "05" -> this.time
            "06" -> this.timeCode
            else -> null
        }
    }
}