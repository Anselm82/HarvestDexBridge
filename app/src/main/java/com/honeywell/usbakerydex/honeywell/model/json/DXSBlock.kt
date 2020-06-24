package com.honeywell.usbakerydex.honeywell.model.json

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonProperty
import com.honeywell.usbakerydex.dex.model.vo.FunctionalIdentifier
import com.honeywell.usbakerydex.dex.model.vo.TestIndicator

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
data class DXSBlock(
    @JsonProperty("RetailerCommunicationID")
    val retailerCommunicationId: String?, //01
    @JsonProperty("FunctionalIdentifierCode")
    val functionalIdentifierCode: String? = FunctionalIdentifier.DX, //02
    @JsonProperty("VersionOrReleaseOrIndustryIdentifierCode")
    val versionOrReleaseOrIndustryIdentifierCode: String?, //03 dex version String
    @JsonProperty("TransmissionControlNumber")
    val transmissionControlNumber: Int?, //04
    @JsonProperty("SupplierCommunicationID")
    val supplierCommunicationsId: String?, //05
    @JsonProperty("TestIndicator")
    val testIndicator: String? = TestIndicator.PRODUCTION //06
)