package com.honeywell.usbakerydex.versatiledex.model

import com.honeywell.usbakerydex.versatiledex.utils.*
import com.honeywell.usbakerydex.versatiledex.utils.isAlphanumeric
import com.honeywell.usbakerydex.versatiledex.utils.isNumeric
import com.honeywell.usbakerydex.versatiledex.utils.validLength

data class Customer(private var duns: String, private var location: String, private var commId: String?,
                    private var number: String? = null, private var name: String? = null, private var address: String? = null,
                    private var city: String? = null, private var state: String? = null, private var zip: String? = null,
                    private var phone1: String? = null, private var phone2: String? = null, private var contact: String? = null, private var notes: String? = null) {

    companion object {
        const val SECTION = "Customer"
        private const val PREFIX = "CUST_"
        const val DUNS = "${PREFIX}DUNS"
        const val LOCATION = "${PREFIX}LOCATION"
        const val COMM_ID = "${PREFIX}COMM_ID"

        // Unused in embedded mode
        const val NUMBER = "${PREFIX}NUMBER"
        const val NAME = "${PREFIX}NAME"
        const val ADDRESS = "${PREFIX}ADDR"
        const val CITY = "${PREFIX}CITY"
        const val STATE = "${PREFIX}STATE"
        const val ZIP = "${PREFIX}ZIP"
        const val PHONE1 = "${PREFIX}PHONE1"
        const val PHONE2 = "${PREFIX}PHONE2"
        const val CONTACT = "${PREFIX}CONTACT"
        const val NOTES = "${PREFIX}NOTES"
    }

    private fun validDuns() = duns.validLength(9) && duns.isNumeric()
    private fun validLocation() = location.validLength(6) && location.isAlphanumeric()
    private fun validCommId() = !commId.isNullOrBlank() && commId!!.validLength(10) && commId!!.isNumeric()

    private fun validNumber() = !number.isNullOrBlank() && number!!.validLength(10) && number!!.isNumeric()
    private fun validName() = !number.isNullOrBlank() && name!!.validLength(30) && name!!.isAlphanumeric()
    private fun validAddress() = !address.isNullOrBlank() && address!!.validLength(30) && address!!.isAlphanumeric()
    private fun validCity() = !city.isNullOrBlank() && city!!.validLength(20) && city!!.isAlphanumeric()
    private fun validState() = !state.isNullOrBlank() && state!!.validLength(2) && state!!.isAlphanumeric()
    private fun validZip() = !zip.isNullOrBlank() && zip!!.validLength(2) && zip!!.isAlphanumeric()
    private fun validPhone1() = !phone1.isNullOrBlank() && phone1!!.validLength(16) && phone1!!.isAlphanumeric()
    private fun validPhone2() = !phone2.isNullOrBlank() && phone2!!.validLength(16) && phone2!!.isAlphanumeric()
    private fun validContact() = !contact.isNullOrBlank() && contact!!.validLength(30) && contact!!.isAlphanumeric()
    private fun validNotes() = !notes.isNullOrBlank() && notes!!.validLength(50) && notes!!.isAlphanumeric()

    private fun isMandatoryDataSetAndValid() = validDuns() && validLocation()

    override fun toString(): String {
        if(isMandatoryDataSetAndValid()) {
            var customer = ""
            customer += "$DUNS $duns$NEW_LINE"
            customer += "$LOCATION \"$location\"$NEW_LINE"
            if (validCommId())
                customer += "$COMM_ID $commId$NEW_LINE"
            if(validNumber())
                customer += "$NUMBER $number$NEW_LINE"
            if(validName())
                customer += "$NAME \"$name\"$NEW_LINE"
            if(validAddress())
                customer += "$ADDRESS \"$address\"$NEW_LINE"
            if(validCity())
                customer += "$CITY \"$city\"$NEW_LINE"
            if(validState())
                customer += "$STATE \"$state\"$NEW_LINE"
            if(validZip())
                customer += "$ZIP \"$zip\"$NEW_LINE"
            if(validPhone1())
                customer += "$PHONE1 \"$phone1\"$NEW_LINE"
            if(validPhone2())
                customer += "$PHONE2 \"$phone2\"$NEW_LINE"
            if(validContact())
                customer += "$CONTACT \"$contact\"$NEW_LINE"
            if(validNotes())
                customer += "$NOTES \"$notes\"$NEW_LINE"
            return customer
        } else {
            throw MandatoryFieldException(SECTION)
        }
    }
}