package com.honeywell.usbakerydex.dex.model.vo

class CreditDebitFlagCode {
    companion object {
        const val CREDIT = "C"
        const val DEBIT = "D"

        fun values(): List<String> {
            return arrayListOf(CREDIT, DEBIT)
        }
    }
}