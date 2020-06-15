package com.honeywell.usbakerydex.versatiledex.model

enum class VersatileDexMode(val value: Int) {
    ACTION_START_DEX(0),
    ACTION_LICENSE_REFRESH(1),
    ACTION_START_ACTIVATE(2),
    ACTION_LICENSE_STATUS(3)
}