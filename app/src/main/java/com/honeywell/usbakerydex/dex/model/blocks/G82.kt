package com.honeywell.usbakerydex.dex.model.blocks

import com.honeywell.usbakerydex.dex.model.toYYYYMMDD
import com.honeywell.usbakerydex.dex.model.vo.CreditDebitFlagCode
import com.honeywell.usbakerydex.dex.model.vo.ShipmentMethodOfPayment
import com.honeywell.usbakerydex.dex.model.withLeadingZeros
import java.util.*


/**
 * if creditDebitFlagCode == D this is a delivery, else it is a return
 */
data class G82(
    var creditDebitFlagCode: String? = null, //01  M-1
    var supplierDeliveryReturnNumber: String? = null, //02,    M-[1-22] TODO(copy to g8703) unique inside DXS
    var receiverDunsNumber: String? = null, //03   O-[9-9] Communicated to the supplier in advance
    var receiverLocationNumber: String? = null, //04    M-[1-13]  Usually, DUNS + 4 left-justified numbers of the location or GLN
    var supplierDunsNumber: String? = null, //05   O-[9-9] Communicated to the retailer in advance
    var supplierLocationNumber: String? = null, //06    M-[1-13]  Usually, DUNS + 4 left-justified numbers of the location (route, driver,...) it may change without notice,
    var physicalDeliveryOrReturnDate: String? = Date().toYYYYMMDD(), //07    M-[8/8] CCYYMMDD
    var productOwnershipTransferDate: String? = null, //08    O-[8/8] CCYYMMDD
    var purchaseOrderNumber: String? = null, //09  O-[1-22]
    var purchaseOrderDate: String? = null, //10    O-[8-8] CCYYMMDD
    var shipmentMethodOfPayment: String? = null, //11  O-[2-2]
    var methodOfPaymentCode: String? = null //12    O-[1] if present, the payment must be made on delivery
) {
    fun putString(key: String, value: String?) {
        when (key) {
            "01" -> this.creditDebitFlagCode =
                if (value.isNullOrBlank() || value !in CreditDebitFlagCode.values()) null else value
            "02" -> this.supplierDeliveryReturnNumber = value
            "03" -> this.receiverDunsNumber = withLeadingZeros(value, 9)
            "04" -> this.receiverLocationNumber = value
            "05" -> this.supplierDunsNumber = withLeadingZeros(value, 9)
            "06" -> this.supplierLocationNumber = value
            "07" -> this.physicalDeliveryOrReturnDate = value
            "08" -> this.productOwnershipTransferDate = value
            "09" -> this.purchaseOrderNumber = value
            "10" -> this.purchaseOrderDate = value
            "11" -> this.shipmentMethodOfPayment =
                if (value.isNullOrBlank() || value !in ShipmentMethodOfPayment.values()) null else value
            "12" -> this.methodOfPaymentCode = value
        }
    }

    fun getString(key: String): String? {
        return when (key) {
            "01" -> this.creditDebitFlagCode
            "02" -> this.supplierDeliveryReturnNumber
            "03" -> this.receiverDunsNumber
            "04" -> this.receiverLocationNumber
            "05" -> this.supplierDunsNumber
            "06" -> this.supplierLocationNumber
            "07" -> this.physicalDeliveryOrReturnDate
            "08" -> this.productOwnershipTransferDate
            "09" -> this.purchaseOrderNumber
            "10" -> this.purchaseOrderDate
            "11" -> this.shipmentMethodOfPayment
            "12" -> this.methodOfPaymentCode
            else -> null
        }
    }
}