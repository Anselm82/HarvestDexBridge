package com.honeywell.usbakerydex.versatiledex.utils

import com.honeywell.usbakerydex.VersatileConverter
import java.lang.Exception

const val NEW_LINE = "\n"
const val EMPTY_SPACE = " "
const val PERCENTAGE_SPLITER = "/"
const val UNLIMITED_LENGHT = 0

class MandatoryFieldException(section: String) :
    Exception("A value in $section is missing or invalid.")

class InvalidValueException(section: String) : Exception("Error when parsing a value in $section.")

enum class HoneywellSections(val value: String) {
    CONFIG("config"),
    TRANSACTION("transaction"),
    INITIALIZATION("initialization"),
    TRANSMISSION_CONTROL_NUMBER("transmissioncontrolnumber"),
    TRANSACTION_CONTROL_NUMBER("transactionsetcontrolnumber")
}

class VersatileResponseParams {
    companion object {
        const val ITEM_INDEX = "ITEM_INDEX"
        const val OLD_VALUE = "OLD_VALUE"
        const val NEW_VALUE = "NEW_VALUE"
        const val RATE = "RATE"
        const val ADJ_CODE = "ADJ_CODE"
        const val REASON = "REASON"
        const val INVOICE_STATUS = "INVOICE_STATUS"
        const val UPC = "UPC"
        const val CASE_UPC = "CASE_UPC"
        const val PROD_QUALIFIER = "PROD_QUALIFIER"
        const val PROD_ID_QUALIFIER = "PROD_ID_QUALIFIER"
        const val PROD_ID = "PROD_ID"
        const val QTY = "QTY"
        const val PACKTYPE = "PACKTYPE"
        const val PRICE = "PRICE"
        const val PACK = "PACK"
        const val INNER_PACK = "INNER_PACK"
        const val CASE_QUALIFIER = "CASE_QUALIFIER"
        const val CASE_ID = "CASE_ID"
        const val UNIT_OF_MEASURE = "UNIT_OF_MEASURE"
        const val ITEM_LIST_COST = "ITEM_LIST_COST"
    }
}

enum class VersatileResponseInvoiceStatus(val value: Int) {
    UNSENT(0),
    SENT(1),
    RECEIVED(2),
    CLOSED(3);

    companion object {
        fun fromValue(value: Int): VersatileResponseInvoiceStatus {
            return values().first { item -> item.value == value }
        }
    }
}

enum class VersatileResponseAdjustmentType(val value: String) {
    ADJ_PACKTYPE("ADJ_PACKTYPE"),
    ADJ_PRICE("ADJ_PRICE"),
    ADJ_QTY("ADJ_QTY"),
    ADJ_UPC("ADJ_UPC"), //DEX version <= 4010
    ADJ_CASEUPC("ADJ_CASEUPC"), //DEX version <= 4010
    ADJ_PACK("ADJ_PACK"),
    ADJ_INNERPACK("ADJ_INNERPACK"),
    ADJ_PROD_QUALIFIER("ADJ_PROD_QUALIFIER"),
    ADJ_PROD_ID("ADJ_PROD_ID"),
    ADJ_ALLOWANCE("ADJ_ALLOWANCE"),
    ADJ_ALLOWANCE_REJECTED("ADJ_ALLOWANCE_REJECTED"),
    ADJ_CHARGE("ADJ_CHARGE"),
    ADJ_CHARGE_REJECTED("ADJ_CHARGE_REJECTED"),
    ADJ_KILL_PREVIOUS_ALLOW_CHG("ADJ_KILL_PREVIOUS_ALLOW_CHG"),
    ADJ_NEW_ITEM("ADJ_NEW_ITEM"),
    ADJ_NEW_ITEM_REJECTED("ADJ_NEW_ITEM_REJECTED"),
    ADJ_DEL_ITEM("ADJ_DEL_ITEM"),
    INVC_STATUS_MANUALLY_CHANGED("INVC_STATUS_MANUALLY_CHANGED"),
    INVC_STATUS("INVC_STATUS"),

    ADJ_INVC_ALLOWANCE("ADJ_INVC_ALLOWANCE"),
    ADJ_INVC_CHARGE("ADJ_INVC_CHARGE"),
    ADJ_INVC_KILL_PREVIOUS_ALLOW_CHG("ADJ_INVC_KILL_PREVIOUS_ALLOW_CHG"),

    ADJ_D_R_DATE("ADJ_D_R_DATE"),
    ADJ_POTD("ADJ_POTD"),
    ADJ_PONUM("ADJ_PONUM"),
    ADJ_PODATE("ADJ_PODATE"),
    ADJ_LOCATION("ADJ_LOCATION")
}

enum class VersatileResponseCode(val value: String) {
    USR("USR"),
    SVR("SVR")
}

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

internal fun String.extractVersatileBooleanValue(): String {
    val char = this.substring(0, 1)
    val trueValue = arrayOf("T", "Y", "0")
    return if (trueValue.indexOf(char.toUpperCase()) > 0) "Y" else "N"
}

internal fun String.isAlphanumeric(): Boolean {
    val alphanumericExpression = "^[a-zA-Z0-9 ]+\$".toRegex()
    return (this.matches(alphanumericExpression))
}

internal fun String.isAlphabetical(): Boolean {
    val alphaExpression = "^[a-zA-Z]+\$".toRegex()
    return (this.matches(alphaExpression))
}

internal fun String.isNumeric(): Boolean {
    val numericExpression = "^[0-9][.]+\$".toRegex()
    return (this.matches(numericExpression))
}

internal fun String.isDecimal(): Boolean {
    val decimalExpression = "^\\d+(\\.\\d{1,2})?\$".toRegex()
    return (this.matches(decimalExpression))
}

internal fun String.isBoolean(): Boolean {
    val booleanExpression = "^[01FTYN].*\$".toRegex()
    return (this.matches(booleanExpression))
}

internal fun String.validLength(max: Int): Boolean {
    if (max == UNLIMITED_LENGHT)
        return true
    return this.length <= max
}

internal fun String.validLength(min: Int, max: Int): Boolean {
    if (min == UNLIMITED_LENGHT && (max >= this.length || max == UNLIMITED_LENGHT))
        return true
    return this.length in min..max
}

fun main(args: Array<String>) {
    val response = "140701:015830 894:USR 1007 ADJ_QTY 2 1 2\n" +
            "140701:015831 894:USR 1007 ADJ_QTY 4 1 4\n" +
            "140701:015832 895:SVR 1007 ADJ_LOCATION 102 100\n" +
            "140701:015832 895:USR 1007 INVC_STATUS 3"
    val result = VersatileConverter.toVersatileDexResponse(response, 5010)
    result.lines.forEach { item -> print(item.toString()) }
}