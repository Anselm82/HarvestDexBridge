package com.honeywell.usbakerydex.honeywell.model.json

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonProperty

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
data class InvoiceEntry(
    @JsonProperty("Invoice Status")
    val invoiceStatus: InvoiceStatus,
    @JsonProperty("ST")
    val stBlock: STBlock?,
    @JsonProperty("G89")
    val g89Blocks: List<G89Block>?,
    @JsonProperty("G87")
    val g87Block: G87Block?,
    @JsonProperty("G86")
    val g86Block: G86Block?,
    @JsonProperty("G85")
    val g85Block: G85Block?,
    @JsonProperty("G84")
    val g84Block: G84Block?,
    @JsonProperty("SE")
    val seBlock: SEBlock?
)