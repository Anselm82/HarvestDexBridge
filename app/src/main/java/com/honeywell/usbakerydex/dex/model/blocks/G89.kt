package com.honeywell.usbakerydex.dex.model.blocks

import com.honeywell.usbakerydex.dex.model.vo.CaseIDQualifier
import com.honeywell.usbakerydex.dex.model.vo.ProductIDQualifier
import com.honeywell.usbakerydex.dex.model.vo.UOM

data class G89(
    var sequenceNumber: Int? = 1, //01  M1/4 sequential and used as reference in 895 corresponds to 8301
    var quantity: Double?,  //02    R3 1/15 item count optional unless new item is added
    var unitOfMeasure: String? = UOM.EACH, //03 M-2/2 used with G8302 to specify the qty optional unless new item is added
    var upc: String?, //04 UPC Unused in latest version, uses 05 and 06 instead
    var productIDQualifier: String? = null, //05 M-2/2 used with G8906, only needed if there is some change on it corresponding to 8305
    var productID: String? = null, //06 X[1-48] only needed if there is some change on it corresponding to 8306
    var upcCaseCode: String? = null, //07 UPC Case Unused in latest version. Uses 11 and 12 instead
    var itemListCost: Double? = 0.0, //08 O-[1,9] gross price per 8303, always positive. Extended list cost is g8302 * g8308
    var pack: Int? = null, //09 X[1-6] number of eaches or inner containers, items or items in a case
    var innerPack: Int? = null, //10 inner pack [1-20]
    var caseIDQualifier: String? = null, //11  M-2/2 used with G8911  only needed if there is some change on it corresponding to 8311
    var caseID: String? = null //12 X[1-48]  only needed if there is some change on it corresponding to 8312
) : ItemDetailRecord {

    init {
        putString("05", this.productIDQualifier)
        putString("11", this.caseIDQualifier)
    }

    fun putInt(key: String, value: Int?) {
        when (key) {
            "01" -> this.sequenceNumber = value
            "09" -> this.pack = value
            "10" -> this.innerPack = value
        }
    }

    fun getInt(key: String): Int? {
        return when (key) {
            "01" -> this.sequenceNumber
            "09" -> this.pack
            "10" -> this.innerPack
            else -> null
        }
    }

    fun putDouble(key: String, value: Double?) {
        when (key) {
            "02" -> this.quantity = value
            "08" -> this.itemListCost = value
        }
    }

    fun getDouble(key: String): Double? {
        return when (key) {
            "02" -> this.quantity
            "08" -> this.itemListCost
            else -> null
        }
    }

    fun putString(key: String, value: String?) {
        when (key) {
            "03" -> this.unitOfMeasure = value
            "04" -> this.upc = value
            "05" -> this.productIDQualifier =
                if (value.isNullOrBlank() || value !in ProductIDQualifier.values()) null else value
            "06" -> this.productID = value
            "07" -> this.upcCaseCode = value
            "11" -> this.caseIDQualifier =
                if (value.isNullOrBlank() || value !in CaseIDQualifier.values()) null else value
            "12" -> this.caseID = value
        }
    }

    fun getString(key: String): String? {
        return when (key) {
            "03" -> this.unitOfMeasure
            "04" -> this.upc
            "05" -> this.productIDQualifier
            "06" -> this.productID
            "07" -> this.upcCaseCode
            "11" -> this.caseIDQualifier
            "12" -> this.caseID
            else -> null
        }
    }
}