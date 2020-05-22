package com.honeywell.usbakerydex.versatiledex.utils

import java.lang.Exception

const val NEW_LINE = "\n"
const val PERCENTAGE_SPLITER = "/"
const val UNLIMITED_LENGHT = 0

class MandatoryFieldException(section: String): Exception("A value in $section is missing or invalid.")
class InvalidValueException(section: String): Exception("Error when parsing a value in $section.")

enum class VersatilePackType(value: String) {
    CASE("CA"),
    EACH("EA")
}

enum class VersatileOrderType(val value: String) {
    DELIVERY("DELIVERY"),
    RETURN("RETURN")
}

enum class VersatileAdjustmentType(val value: Char) {
    ALLOWANCE('A'),
    CHARGE('C')
}

enum class VersatileHandlingCode(val value: String) {
    BILL_BACK("01"),
    OFF_INVOICE("02"),
    INFO_ONLY("15")
}

enum class VersatileInvoiceAdjustmentFlag(val value: Char) {
    TOTAL('T'),
    PERCENTAGE('%')
}

enum class VersatileAdjustmentFlag(val value: Char) {
    TOTAL('T'),
    PERCENTAGE('%'),
    RATE_PER_QUANTITY('$')
}

internal fun String.extractVersatileBooleanValue() : String {
    val char = this.substring(0, 1)
    val trueValue = arrayOf("T", "Y", "0")
    return if (trueValue.indexOf(char.toUpperCase()) > 0) "Y" else "N"
}

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
    val decimalExpression = "^\\d+(\\.\\d{1,2})?\$".toRegex()
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

internal fun String.validLength(min: Int, max: Int) : Boolean {
    if(min == UNLIMITED_LENGHT && (max >= this.length || max == UNLIMITED_LENGHT))
        return true
    return this.length in min..max
}

fun main(args: Array<String>) {
    println("1234567890".validLength(8, 10))
    println("123456789".validLength(0, 10))
    println("123456789".validLength(0, 0))
    println("123456789".validLength(4, 6))
}