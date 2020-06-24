package com.honeywell.usbakerydex.honeywell.model.json

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonProperty
import com.honeywell.usbakerydex.dex.model.DEXTransmission
import com.honeywell.usbakerydex.honeywell.model.json.ReceiveDexData
import org.json.JSONObject

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
data class HoneywellDexResponse(
    @JsonProperty("ReceiveDexData")
    var receiveDexData: ReceiveDexData? = null
)