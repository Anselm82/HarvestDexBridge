package com.honeywell.usbakerydex.honeywell.model.json

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonProperty

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
data class G72Block(
    @JsonProperty("AllowanceCode")
    var allowanceOrChargeCode: Int, //01
    @JsonProperty("MethodOfHandling")
    var handlingMethod: String?, //02
    @JsonProperty("AllowanceNumber")
    var allowanceOrChargeNumber: String?, //03
    var exceptionNumber: Double? = null, //04
    @JsonProperty("AllowanceRate")
    var allowanceOrChargeRate: Double? = null, //05
    @JsonProperty("AllowanceQuantity")
    var allowanceOrChargeQty: String? = null, //06
    @JsonProperty("UOMCode")
    var unitOfMeasure: String? = null //07
)