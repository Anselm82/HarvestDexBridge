package com.honeywell.usbakerydex.honeywelldex.model

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonProperty

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
data class DxsBlock(
    @JsonProperty("RetailerCommunicationID")
    val retailerCommunicationId: String?, //01
    @JsonProperty("FunctionalIdentifierCode")
    val functionalIdentifierCode: String? = FunctionalIdentifier.DX.value, //02
    @JsonProperty("VersionOrReleaseOrIndustryIdentifierCode")
    val versionOrReleaseOrIndustryIdentifierCode: String?, //03 dex version String
    @JsonProperty("TransmissionControlNumber")
    val transmissionControlNumber: Long?, //04
    @JsonProperty("SupplierCommunicationID")
    val supplierCommunicationsId: String?, //05
    @JsonProperty("TestIndicator")
    val testIndicator: String? = TestIndicator.PRODUCTION.value //06
)