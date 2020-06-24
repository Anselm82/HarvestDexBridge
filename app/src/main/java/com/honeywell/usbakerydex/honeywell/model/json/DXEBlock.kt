package com.honeywell.usbakerydex.honeywell.model.json

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonProperty

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
data class DXEBlock(
    @JsonProperty("TransmissionControlNumber")
    val transmissionControlNumber: Int?, //01 same as DXS 04
    @JsonProperty("NumberOfTransactionSetsIncluded")
    val numberOfIncludedSets: Int? = 0
)