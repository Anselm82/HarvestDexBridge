package com.honeywell.usbakerydex.versatiledex.model

import com.honeywell.usbakerydex.dex.model.ACK_ADJ_RECORD
import com.honeywell.usbakerydex.dex.model.vo.InitiatorCode
import com.honeywell.usbakerydex.versatiledex.utils.VersatileResponseAdjustmentType
import com.honeywell.usbakerydex.versatiledex.utils.VersatileResponseCode
import com.honeywell.usbakerydex.versatiledex.utils.VersatileResponseParams

data class VersatileDexResponse(val lines: Array<VersatileDexResponseEntry>) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as VersatileDexResponse
        if (!lines.contentEquals(other.lines)) return false
        return true
    }

    override fun hashCode(): Int {
        return lines.contentHashCode()
    }

    override fun toString(): String {
        return lines.joinToString { line -> line.toString() }
    }

    fun invoices() : Map<String, List<VersatileDexResponseEntry>> {
        return lines.groupBy { it.invoice }
    }

    fun invoice(invoiceNumber: String) : List<VersatileDexResponseEntry>? {
        return invoices()[invoiceNumber]
    }

    fun invoiceResponse(invoiceNumber: String) : List<VersatileDexResponseEntry>?  {
        return invoice(invoiceNumber)?.filter { "${it.ucsType}" == ACK_ADJ_RECORD }
    }

    fun initiator(invoiceNumber: String) : VersatileResponseCode {
        val invoiceStatus = invoice(invoiceNumber)?.filter { it.adjustmentType == VersatileResponseAdjustmentType.INVC_STATUS }?.get(0)
        return if("${invoiceStatus!!.ucsType}" == ACK_ADJ_RECORD) VersatileResponseCode.SVR else VersatileResponseCode.USR
    }

    fun status(invoiceNumber: String) : String? {
        return invoice(invoiceNumber)?.filter { it.adjustmentType == VersatileResponseAdjustmentType.INVC_STATUS }
            ?.get(0)?.params?.get(VersatileResponseParams.INVOICE_STATUS) as String?
    }

    fun itemAdjustments(invoiceNumber: String) : List<VersatileDexResponseEntry>? {
        return invoice(invoiceNumber)?.filter { it.adjustmentType in VersatileResponseAdjustmentType.itemAdjustmentTypes() }
    }
}

