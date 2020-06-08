package com.honeywell.usbakerydex.honeywelldex.model

data class DxsBlock(val communicationIdNumber: String?, //01
                    val functionalIdentifier: String?, //02
                    val version: String?, //03 dex version String
                    val transmissionControlNumber: Int, //04
                    val communicationsId: String?, //05
                    val testIndicator: String? //06
)