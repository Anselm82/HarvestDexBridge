package com.honeywell.usbakerydex.honeywelldex.model

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonProperty
import com.honeywell.usbakerydex.honeywelldex.utils.HoneywellHandlingMethodCodes

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
data class G72Block(
    @JsonProperty("AllowanceCode")
    var allowanceOrChargeCode: Int, //01
    @JsonProperty("MethodOfHandling")
    var handlingMethod: HoneywellHandlingMethodCodes, //02
    @JsonProperty("AllowanceNumber")
    var allowanceOrChargeNumber: String?, //03
    var exceptionNumber: String? = null, //04
    @JsonProperty("AllowanceRate")
    var allowanceOrChargeRate: Double? = null, //05
    @JsonProperty("AllowanceQuantity")
    var allowanceOrChargeQty: Double? = null, //06
    @JsonProperty("UOMCode")
    var unitOfMeasure: String? = null, //07

    var allowanceOrChargeTotalAmount: Double? = null, //08

    var allowanceOrChargePercent: Double? = null, //09

    var dollarBasisForPercent: Double? = null, //10

    var optionNumber: String? = null //11
)