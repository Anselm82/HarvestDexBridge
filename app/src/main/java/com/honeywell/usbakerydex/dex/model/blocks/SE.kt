package com.honeywell.usbakerydex.dex.model.blocks


data class SE(
    var numberOfIncludedSegments: Int? = 0, //01  M-[1-10] segments
    var transactionControlNumber: String? = null //02   M-[4-9] TODO(Copy from ST02)
)