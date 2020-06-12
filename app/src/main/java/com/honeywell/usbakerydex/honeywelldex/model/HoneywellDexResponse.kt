package com.honeywell.usbakerydex.honeywelldex.model

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonProperty

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
data class HoneywellDexResponse(
    @JsonProperty("ReceiveDexData")
    var receiveDexData : ReceiveDexData? = null
)