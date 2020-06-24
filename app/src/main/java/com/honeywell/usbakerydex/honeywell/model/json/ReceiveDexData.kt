package com.honeywell.usbakerydex.honeywell.model.json

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonProperty

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
data class ReceiveDexData(
    @JsonProperty("DXS")
    val DXSBlock: DXSBlock? = null,
    @JsonProperty("InvoiceList")
    val invoiceEntriesList: List<InvoiceEntry>,
    @JsonProperty("DXE")
    val DXEBlock: DXEBlock? = null
)