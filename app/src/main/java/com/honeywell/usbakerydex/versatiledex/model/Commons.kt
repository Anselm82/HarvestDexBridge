package com.honeywell.usbakerydex.versatiledex.model

import java.lang.Exception

const val NEW_LINE = "\n"

const val UNLIMITED_LENGHT = 0

class MandatoryFieldException(field: String, section: String): Exception("The $field in $section is missing or invalid.")

internal fun String.isAlphanumeric() : Boolean {
    val alphanumericExpression = "^[a-zA-Z0-9 ]+\$".toRegex()
    return (this.matches(alphanumericExpression))
}

internal fun String.isAlphabetical() : Boolean {
    val alphaExpression = "^[a-zA-Z]+\$".toRegex()
    return (this.matches(alphaExpression))
}

internal fun String.isNumeric() : Boolean {
    val numericExpression = "^[0-9][.]+\$".toRegex()
    return (this.matches(numericExpression))
}

internal fun String.isDecimal() : Boolean {
    val decimalExpression = "^\\d+(\\.\\d+)?\$".toRegex()
    return (this.matches(decimalExpression))
}

internal fun String.isBoolean() : Boolean {
    val booleanExpression = "^[01FTYN].*\$".toRegex()
    return (this.matches(booleanExpression))
}

internal fun String.validLength(max: Int) : Boolean {
    if(max == UNLIMITED_LENGHT)
        return true
    return this.length <= max
}