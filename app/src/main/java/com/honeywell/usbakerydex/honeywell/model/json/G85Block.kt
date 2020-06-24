package com.honeywell.usbakerydex.honeywell.model.json

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonProperty

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
data class G85Block(
    @JsonProperty("IntegrityCheckValue")
    val integrityCheck: String?
) //01