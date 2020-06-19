package com.honeywell.usbakerydex.dex.model.blocks

data class LoopInnerBlock(
    var itemDetail: ItemDetailRecord?, //Line item detail/direct store delivery O-1 may be G83 or G89
    var g22: G22? = null, //Pre-pricing information O-1
    var itemG72s: List<G72>?, //Allowance or charge O-10
    var itemG23s: List<G23>? = null, //Terms of sale   O-20 UNSUPPORTED
    var loop: Loop? = null
)