package com.honeywell.usbakerydex.honeywelldex.model

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonProperty

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
data class G84Block(
    @JsonProperty("TotalQuantity")
    val quantity: Double?, //01
    @JsonProperty("TotalInvoiceAmount")
    val totalInvoiceAmount: Double?, //02
    val totalDepositDollarAmount: Double? //03
)