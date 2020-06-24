package com.honeywell.usbakerydex.honeywell.model.json

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonProperty

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
data class InvoiceStatus(
    @JsonProperty("InvoiceNumber")
    val invoiceNumber: String,
    @JsonProperty("Status")
    val status: String
)