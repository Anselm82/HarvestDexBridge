package com.honeywell.usbakerydex.dex.model.blocks

import com.honeywell.usbakerydex.dex.model.DEFAULT_LOOP_ID

data class LoopInnerBlock(
    var g83prG89: ItemDetailRecord?, //Line item detail/direct store delivery O-1
    var g22: G22? = null, //Pre-pricing information O-1
    var itemG72s: List<G72>?, //Allowance or charge O-10
    var itemG23s: List<G23>? = null, //Terms of sale   O-20 UNSUPPORTED
    var loop: Loop? = null
)

data class Loop(
    var loopInnerBlocks: List<LoopInnerBlock>? = null,
    var ls: LS? = null, //Loop header   O-1     repeat 9999
    var le: LE? = null //Loop trailer  If LS, M-1
)

data class LS(var loopIdentifierCode: String? = DEFAULT_LOOP_ID) //01 M-[1-4]

data class LE(var loopIdentifierCode: String? = DEFAULT_LOOP_ID) //01 M-[1-4]

