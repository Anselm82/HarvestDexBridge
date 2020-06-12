package com.honeywell.usbakerydex.honeywelldex.model

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonProperty

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
data class ReceiveDexData(
    @JsonProperty("DXS")
    val dxsBlock: DxsBlock? = null,
    @JsonProperty("InvoiceList")
    val invoiceEntriesList: List<InvoiceEntry>,
    @JsonProperty("DXE")
    val dxeBlock: DxeBlock? = null)