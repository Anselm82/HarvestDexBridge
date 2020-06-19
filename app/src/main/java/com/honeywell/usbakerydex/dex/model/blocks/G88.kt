package com.honeywell.usbakerydex.dex.model.blocks

data class G88(
    var physicalDeliveryOrReturnDate: String? = null, //01 O[8] YYYYMMDD taken from 8207
    var productOwnershipTransferDate: String? = null, //02 O[8] YYYYMMDD must have same value than 8208
    var purchaseOrderNumber: String? = null, //03 O[1-22] from 8209
    var purchaseOrderDate: String? = null, //04 O[8] YYYYMMDD G8210
    var receiverLocationNumber: String? = null //05 O[1-6] G8204
) : ExtraInformation {

    fun putString(key: String, value: String?) {
        when (key) {
            "01" -> this.physicalDeliveryOrReturnDate = value
            "02" -> this.productOwnershipTransferDate = value
            "03" -> this.purchaseOrderNumber = value
            "04" -> this.purchaseOrderDate = value
            "05" -> this.receiverLocationNumber = value
        }
    }

    fun getString(key: String): String? {
        return when (key) {
            "01" -> this.physicalDeliveryOrReturnDate
            "02" -> this.productOwnershipTransferDate
            "03" -> this.purchaseOrderNumber
            "04" -> this.purchaseOrderDate
            "05" -> this.receiverLocationNumber
            else -> null
        }
    }

}