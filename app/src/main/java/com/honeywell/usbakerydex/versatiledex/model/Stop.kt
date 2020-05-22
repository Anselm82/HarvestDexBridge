package com.honeywell.usbakerydex.versatiledex.model

import com.honeywell.usbakerydex.versatiledex.utils.*
import com.honeywell.usbakerydex.versatiledex.utils.isNumeric
import com.honeywell.usbakerydex.versatiledex.utils.validLength

data class Stop(private val number: String, private val customer: Customer, private val additionalOptions: AdditionalOptions?, private val invoices: List<Invoice>?) {

    companion object {
        const val SECTION = "Stop"
        const val STOP = "STOP"
    }

    private fun validNumber() = number.validLength(3) && number.isNumeric()

    private fun isMandatoryDataSetAndValid() = validNumber()

    override fun toString(): String {
        if (isMandatoryDataSetAndValid()) {
            var stop = ""
            stop += "$STOP $number$NEW_LINE"
            stop += customer
            stop += additionalOptions
            if(!invoices.isNullOrEmpty())
                stop += invoices.joinToString { invoice -> invoice.toString() }
            return stop
        } else {
            throw MandatoryFieldException(
                SECTION
            )
        }
    }


    data class AdditionalOptions(
        private var dexVersion: String?,
        private var testData: String?,
        private var promptBeforeACK: String?,
        private var ackG88only: String?,
        private var enableG8602: String?,
        private var enable895G84: String?,
        private var enable895G8403: String?,
        private var enable895GAdjOnly: String?,
        private var exclude895G84HCode06: String?,
        private var rejectAdjSvrNewItem: String?,
        private var rejectAdjItemCostReduction: String?,
        private var preserveUPC12: String?
    ) {

        companion object {
            const val SECTION = "Additional stop commands"
            const val DEX_VERSION = "DEX_VERSION"
            const val TEST_DATA = "TEST_DATA"
            const val PROMPT_BEFORE_ACK = "PROMPT_BEFORE_ACK"
            const val ACK_G88_ONLY = "ACK_G88_ONLY"
            const val ENABLE_G8602 = "ENABLE_G8602"
            const val ENABLE_895_G84 = "ENABLE_895_G84"
            const val ENABLE_895_G8403 = "ENABLE_895_G8403"
            const val ENABLE_895_G84_ADJONLY = "ENABLE_895_G84_ADJONLY"
            const val EXCLUDE_895_G84_HCODE_06 = "EXCLUDE_895_G84_HCODE_06"
            const val REJECT_ADJ_SVR_NEW_ITEM = "REJECT_ADJ_SVR_NEW_ITEM"
            const val REJECT_ADJ_ITEM_COST_REDUCTION = "REJECT_ADJ_ITEM_COST_REDUCTION"
            const val PRESERVE_UPC12 = "PRESERVE_UPC12"
        }

        private fun validDexVersion() =
            !dexVersion.isNullOrBlank() && dexVersion!!.validLength(4) && dexVersion!!.isNumeric()

        private fun validTestData() = !testData.isNullOrBlank() && testData!!.isBoolean()
        private fun validPromptBeforeACK() =
            !promptBeforeACK.isNullOrBlank() && promptBeforeACK!!.isBoolean()

        private fun validAckG88only() = !ackG88only.isNullOrBlank() && ackG88only!!.isBoolean()
        private fun validEnableG8602() = !enableG8602.isNullOrBlank() && enable895G84!!.isBoolean()
        private fun validEnable895G84() =
            !enable895G84.isNullOrBlank() && enable895G84!!.isBoolean()

        private fun validEnable895G8403() =
            !enable895G8403.isNullOrBlank() && enable895G8403!!.isBoolean()

        private fun validEnable895GAdjOnly() =
            !enable895GAdjOnly.isNullOrBlank() && enable895GAdjOnly!!.isBoolean()

        private fun validExclude895G84HCode06() =
            !exclude895G84HCode06.isNullOrBlank() && exclude895G84HCode06!!.isBoolean()

        private fun validRejectAdjSvrNewItem() =
            !rejectAdjSvrNewItem.isNullOrBlank() && rejectAdjSvrNewItem!!.isBoolean()

        private fun validRejectAdjItemCostReduction() =
            !rejectAdjItemCostReduction.isNullOrBlank() && rejectAdjItemCostReduction!!.isBoolean()

        private fun validPreserveUPC12() =
            !preserveUPC12.isNullOrBlank() && preserveUPC12!!.isBoolean()

        override fun toString(): String {
            var additionalOptions = ""
            if (validDexVersion())
                additionalOptions += "$DEX_VERSION $dexVersion$NEW_LINE"
            if (validTestData())
                additionalOptions += "$TEST_DATA ${testData!!.extractVersatileBooleanValue()}$NEW_LINE"
            if (validPromptBeforeACK())
                additionalOptions += "$PROMPT_BEFORE_ACK ${promptBeforeACK!!.extractVersatileBooleanValue()}$NEW_LINE"
            if (validAckG88only())
                additionalOptions += "$ACK_G88_ONLY ${ackG88only!!.extractVersatileBooleanValue()}$NEW_LINE"
            if (validEnableG8602())
                additionalOptions += "$ENABLE_G8602 ${enableG8602!!.extractVersatileBooleanValue()}$NEW_LINE"
            if (validEnable895G84())
                additionalOptions += "$ENABLE_895_G84 ${enable895G84!!.extractVersatileBooleanValue()}$NEW_LINE"
            if (validEnable895G8403())
                additionalOptions += "$ENABLE_895_G8403 ${enable895G8403!!.extractVersatileBooleanValue()}$NEW_LINE"
            if (validEnable895GAdjOnly())
                additionalOptions += "$ENABLE_895_G84_ADJONLY ${enable895GAdjOnly!!.extractVersatileBooleanValue()}$NEW_LINE"
            if (validExclude895G84HCode06())
                additionalOptions += "$EXCLUDE_895_G84_HCODE_06 ${exclude895G84HCode06!!.extractVersatileBooleanValue()}$NEW_LINE"
            if (validRejectAdjSvrNewItem())
                additionalOptions += "$REJECT_ADJ_SVR_NEW_ITEM ${rejectAdjSvrNewItem!!.extractVersatileBooleanValue()}$NEW_LINE"
            if (validRejectAdjItemCostReduction())
                additionalOptions += "$REJECT_ADJ_ITEM_COST_REDUCTION ${rejectAdjItemCostReduction!!.extractVersatileBooleanValue()}$NEW_LINE"
            if (validPreserveUPC12())
                additionalOptions += "$PRESERVE_UPC12 ${preserveUPC12!!.extractVersatileBooleanValue()}$NEW_LINE"
            return additionalOptions
        }
    }
}