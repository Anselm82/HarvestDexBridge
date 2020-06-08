package com.honeywell.usbakerydex.honeywelldex.model

data class Invoice(
    val st: STBlock,
    val g82: G82Block,
    val g84: G84Block,
    val adjustments: Map<String, G72Block?>?,
    val items: Map<String, G83Block?>?,
    val n9: N9Block?,
    val mtx: MtxBlock?
)