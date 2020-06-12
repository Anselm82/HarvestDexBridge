package com.honeywell.usbakerydex.honeywelldex.model

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonProperty

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
data class DxeBlock(
    @JsonProperty("TransmissionControlNumber")
    val transmissionControlNumber: Long, //01 same as DXS 04
    @JsonProperty("NumberOfTransactionSetsIncluded")
    val numberOfIncludedSets: Int = 0)