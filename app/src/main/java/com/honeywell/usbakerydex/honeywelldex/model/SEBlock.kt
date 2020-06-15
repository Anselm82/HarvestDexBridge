package com.honeywell.usbakerydex.honeywelldex.model

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonProperty

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
data class SEBlock(
    @JsonProperty("SegmentCount")
    val segmentCount: Int,
    @JsonProperty("TransactionSetControlNumber")
    val transactionSetControlNumber: Int
)