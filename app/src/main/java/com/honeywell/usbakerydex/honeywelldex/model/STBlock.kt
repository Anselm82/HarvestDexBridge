package com.honeywell.usbakerydex.honeywelldex.model

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonProperty

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
data class STBlock(
    @JsonProperty("TransactionSetID")
    val ucsType: String, //01
    @JsonProperty("TransactionSetControlNumber")
    val transactionIndex: Long? = 0, //02
    val implementationConventionRelease: String? = null //03
)