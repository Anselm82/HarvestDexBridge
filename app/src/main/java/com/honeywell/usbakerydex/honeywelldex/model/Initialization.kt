package com.honeywell.usbakerydex.honeywelldex.model

data class Initialization(
    val communicationMethod: String,
    val eventSourceId: Int,
    val iniFile: String,
    val instanceName: String,
    val syncHType: Int
)