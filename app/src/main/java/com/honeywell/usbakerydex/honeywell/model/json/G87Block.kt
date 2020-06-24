package com.honeywell.usbakerydex.honeywell.model.json

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonProperty

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
data class G87Block(
    @JsonProperty("InitiatorCode")
    val initiatorCode: String?, //01
    @JsonProperty("CreditDebitFlag")
    val creditDebitFlag: String?, //02 must have same value than 8201
    @JsonProperty("SupplierDeliveryReturnNumber")
    val supplierDeliveryOrReturnNumber: String?, //03
    @JsonProperty("IntegrityCheckValue")
    val integrityCheck: String?, //04
    @JsonProperty("AdjustmentNumber")
    val adjustmentNumber: Int?, //05
    @JsonProperty("ReceiverDeliveryReturnNumber")
    val receiverDeliveryOrReturnNumber: String? //06
)