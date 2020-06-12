package com.honeywell.usbakerydex.dex.model

import java.text.SimpleDateFormat
import java.util.*

internal fun withLeadingZeros(value: String?, size: Int): String? {
    if(value.isNullOrBlank())
        return null
    var duns = value
    while(size > duns!!.length)
        duns = "0$duns"
    return duns
}

internal fun Date.toYYYYMMDD() = SimpleDateFormat(DATE_FORMAT, Locale.US).format(this)


class UnsupportedElementException() : Exception("G23 segment is not implemented!")