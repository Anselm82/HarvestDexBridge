package com.honeywell.usbakerydex.honeywelldex.model

import com.honeywell.usbakerydex.honeywelldex.utils.HoneywellTypeFlag

data class G82Block(
    val typeFlag: HoneywellTypeFlag, //01
    val invoiceNumber: String, //02
    val receiverDunsNumber: String? = null, //03
    val receiverLocationNumber: String? = null, //04
    val supplierDunsNumber: String? = null, //05
    val supplierLocationNumber: String? = null, //06
    val deliveryReturnDate: Long? = null, //07 YYYYMMDD
    val ownershipTransferDate: Long? = null, //08 YYYYMMDD
    val purchaseOrderNumber: String? = null, //09
    val purchaseOrderDate: Long? = null, //10 YYYYMMDD
    val shipmentMethodOfPayment: String? = null, //11
    val codMethodOfPayment: String? = null //12
)