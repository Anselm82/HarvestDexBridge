package com.honeywell.usbakerydex.honeywell.model.json

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonProperty
import com.honeywell.usbakerydex.honeywell.model.json.G72Block

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
data class G89Block(
    @JsonProperty("SequenceNumber")
    val sequenceNumber: String?, //01 must have same value as 8301, but when added by retailer, same as 8901 received
    @JsonProperty("Quantity")
    val quantity: Double?, //02
    @JsonProperty("ItemListCost")
    val itemListCost: Double?, //08
    @JsonProperty("G72")
    val g72List: List<G72Block>?
)