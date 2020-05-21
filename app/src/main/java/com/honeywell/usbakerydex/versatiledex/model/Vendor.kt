package com.honeywell.usbakerydex.versatiledex.model

data class Vendor(private var route: String?, private var name: String?, private val duns: String, private val location: String, private val commId: String, private val dexVersion: String) {

    companion object {
        const val ROUTE = "ROUTE"
        const val NAME = "NAME"
        const val DUNS = "DUNS"
        const val LOCATION = "LOCATION"
        const val COMM_ID = "COMM_ID"
        const val DEX_VERSION = "DEX_VERSION"
    }

    private fun validRoute() = !route.isNullOrBlank() && route!!.validLength(15) && route!!.isAlphanumeric()
    private fun validName() = !name.isNullOrBlank() && name!!.validLength(30) && name!!.isAlphanumeric()
    private fun validDuns() = duns.validLength(9) && duns.isNumeric()
    private fun validLocation() = location.validLength(6) && location.isAlphanumeric()
    private fun validCommId() = commId.validLength(10) && commId.isNumeric()
    private fun validDexVersion() = dexVersion.validLength(4) && dexVersion.isNumeric()

    private fun isMandatoryDataSetAndValid() = validDuns() && validCommId() && validDexVersion() && validLocation()

    override fun toString(): String {
        if(isMandatoryDataSetAndValid()) {
            var vendor = ""
            if (validRoute())
                vendor += "$ROUTE \"$route\"$NEW_LINE"
            if (validName())
                vendor += "$NAME \"$name\"$NEW_LINE"
            vendor += "$DUNS \"$duns\"$NEW_LINE"
            vendor += "$LOCATION \"$location\"$NEW_LINE"
            vendor += "$COMM_ID \"$commId\"$NEW_LINE"
            vendor += "$DEX_VERSION \"$dexVersion\"$NEW_LINE"
            return vendor
        } else {
            throw MandatoryFieldException()
        }
    }
}
