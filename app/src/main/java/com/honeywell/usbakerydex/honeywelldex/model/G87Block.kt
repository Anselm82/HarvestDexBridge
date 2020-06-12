package com.honeywell.usbakerydex.honeywelldex.model

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonProperty
import com.honeywell.usbakerydex.honeywelldex.utils.HoneywellInitiatorCode
import com.honeywell.usbakerydex.honeywelldex.utils.HoneywellTypeFlag

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
data class G87Block(
    @JsonProperty("InitiatorCode")
    val initiatorCode: HoneywellInitiatorCode, //01
    @JsonProperty("CreditDebitFlag")
    val creditDebitFlag: HoneywellTypeFlag, //02 must have same value than 8201
    @JsonProperty("SupplierDeliveryReturnNumber")
    val supplierDeliveryOrReturnNumber: String?, //03
    @JsonProperty("IntegrityCheckValue")
    val integrityCheck: String?, //04
    @JsonProperty("AdjustmentNumber")
    val adjustmentNumber: Int?, //05
    @JsonProperty("ReceiverDeliveryReturnNumber")
    val receiverDeliveryOrReturnNumber: String? //06
)