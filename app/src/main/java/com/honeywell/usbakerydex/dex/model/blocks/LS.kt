package com.honeywell.usbakerydex.dex.model.blocks

import com.honeywell.usbakerydex.dex.model.DEFAULT_LOOP_ID

data class LS(var loopIdentifierCode: String? = DEFAULT_LOOP_ID) //01 M-[1-4]