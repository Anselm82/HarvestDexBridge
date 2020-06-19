package com.honeywell.usbakerydex.dex.model.blocks

import com.honeywell.usbakerydex.dex.model.DEFAULT_LOOP_ID

data class Loop(
    var loopInnerBlocks: List<LoopInnerBlock>? = null,
    var ls: LS? = null, //Loop header   O-1     repeat 9999
    var le: LE? = null //Loop trailer  If LS, M-1
)

