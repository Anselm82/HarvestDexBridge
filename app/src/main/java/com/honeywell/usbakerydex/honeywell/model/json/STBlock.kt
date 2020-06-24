package com.honeywell.usbakerydex.honeywell.model.json

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonProperty

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
data class STBlock(
    @JsonProperty("TransactionSetID")
    val ucsType: String, //01
    @JsonProperty("TransactionSetControlNumber")
    val transactionIndex: Long? = 0 //02
)