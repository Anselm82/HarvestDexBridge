package com.honeywell.usbakerydex.versatiledex.model

import com.honeywell.usbakerydex.versatiledex.utils.*
import com.honeywell.usbakerydex.versatiledex.utils.isAlphanumeric
import com.honeywell.usbakerydex.versatiledex.utils.isNumeric

data class Vendor(
    private var route: String?, private var name: String?, val duns: String,
    private val location: String, private val commId: String, val dexVersion: String
) {

    companion object {
        const val SECTION = "Vendor"
        const val ROUTE = "ROUTE"
        const val NAME = "NAME"
        const val DUNS = "DUNS"
        const val LOCATION = "LOCATION"
        const val COMM_ID = "COMM_ID"
        const val DEX_VERSION = "DEX_VERSION"
    }

    private fun validRoute() =
        !route.isNullOrBlank() && route!!.validLength(15) && route!!.isAlphanumeric()

    private fun validName() =
        !name.isNullOrBlank() && name!!.validLength(30) && name!!.isAlphanumeric()

    private fun validDuns() = duns.validLength(9) && duns.isNumeric()
    private fun validLocation() = location.validLength(6) && location.isAlphanumeric()
    private fun validCommId() = commId.validLength(10) && commId.isNumeric()
    private fun validDexVersion() =
        dexVersion.cleanUCS().toInt().toString().validLength(4) && dexVersion.cleanUCS().isNumeric()

    private fun isMandatoryDataSetAndValid() = validDuns() && validCommId() && validLocation()

    override fun toString(): String {
        if (isMandatoryDataSetAndValid()) {
            var vendor = ""
            if (validRoute())
                vendor += "$ROUTE \"$route\"$NEW_LINE"
            if (validName())
                vendor += "$NAME \"$name\"$NEW_LINE"
            vendor += "$DUNS ${paddingWithZero(duns, 9)}$NEW_LINE"
            vendor += "$LOCATION \"$location\"$NEW_LINE"
            vendor += "$COMM_ID $commId$NEW_LINE"
            if (validDexVersion())
                vendor += "$DEX_VERSION ${dexVersion.cleanUCS().toInt()}$NEW_LINE"
            return "$vendor$NEW_LINE"
        } else
            throw MandatoryFieldException(SECTION)
    }
}
