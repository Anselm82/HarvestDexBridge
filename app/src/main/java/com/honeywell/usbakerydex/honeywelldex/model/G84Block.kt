package com.honeywell.usbakerydex.honeywelldex.model

data class G84Block(
    val quantity: Double?, //01
    val totalInvoiceAmount: Double?, //02
    val totalDepositDollarAmount: Double? //03
)