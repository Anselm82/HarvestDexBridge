package com.honeywell.usbakerydex.dex.model.blocks

data class DXE(
    var transmissionControlNumber: Int?, //01 M-[1-5] TODO(Copy from DSX04)
    var numberOfTransactionSetsIncluded: Int? //01 M-[1-6] TODO(Count of ST, at this point always one)
)