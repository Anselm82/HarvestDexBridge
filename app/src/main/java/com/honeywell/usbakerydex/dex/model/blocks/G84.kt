package com.honeywell.usbakerydex.dex.model.blocks

data class G84(
    var quantity: Double? = 0.0, //01    Depends Double R3 [1-15] SUM of g8302 in the record 894
    var totalInvoiceAmount: Double? = 0.0, //02     N2 [1-10] Depends sum of charges (positive), allowances (negative) and deposits.
    var totalDepositDollarAmount: Double? //03  N2 [1-6] Sum of all g83 where G8305 == DI as G8302 * G8308 plus G72 item level where G7201 == 525 as G7205 * (G7206 if used, or G8302 otherwise) expressed as whole value (positive).
) {

    fun putDouble(key: String, value: Double?) {
        when (key) {
            "01" -> this.quantity = value
            "02" -> this.totalInvoiceAmount = value
            "03" -> this.totalDepositDollarAmount = value
        }
    }

    fun getDouble(key: String): Double? {
        return when (key) {
            "01" -> this.quantity
            "02" -> this.totalInvoiceAmount
            "03" -> this.totalDepositDollarAmount
            else -> null
        }
    }
}