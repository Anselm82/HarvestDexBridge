package com.honeywell.usbakerydex.honeywelldex.model

data class Transaction(val dxs: DxsBlock?, val invoices: Map<String, Invoice>, val dxe: DxeBlock?)