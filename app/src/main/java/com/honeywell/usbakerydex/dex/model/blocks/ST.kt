package com.honeywell.usbakerydex.dex.model.blocks

data class ST(
    var transactionSetIdentifierCode: String? = "894", //01, M-[3-3] (894/895)
    var transactionSetControlNumber: String? = null, //02, M-[4-9] Identifier generated by the sender/supplier
    var implementationConventionRelease: String? = null //03 DexVersion
) {
    fun putString(key: String, value: String?) {
        when (key) {
            "01" -> this.transactionSetIdentifierCode = value
            "02" -> this.transactionSetControlNumber = value
            "03" -> this.implementationConventionRelease = value
        }
    }

    fun getString(key: String): String? {
        return when (key) {
            "01" -> this.transactionSetIdentifierCode
            "02" -> this.transactionSetControlNumber
            "03" -> this.implementationConventionRelease
            else -> null
        }
    }
}