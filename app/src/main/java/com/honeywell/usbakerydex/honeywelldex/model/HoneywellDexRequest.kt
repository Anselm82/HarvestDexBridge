package com.honeywell.usbakerydex.honeywelldex.model

data class Retailer(
    val communicationsId: String,
    val dexVersion: String,
    val dunsNumber: String,
    val location: String = "{}"
)

data class Supplier(val signatureKey: Int)
data class Config(
    val retailer: Retailer,
    val supplier: Supplier,
    val transactionSetControlNumber: Int,
    val transmissionControlNumber: Int
)

data class Initialization(
    val communicationMethod: String,
    val eventSourceId: Int,
    val iniFile: String,
    val instanceName: String,
    val syncHType: Int
)

data class Transaction(val invoices: Map<String, Invoice>)
data class Invoice(
    val st: STBlock,
    val g82: G82Block,
    val g84: G84Block,
    val adjustments: Map<String, AdjustmentBlock>,
    val items: Map<String, ItemBlock>,
    val n9: N9Block?
)

data class STBlock(
    val ucsType: Int, //01
    val transactionIndex: Long? = 0, //02
    val implementationConventionRelease: String? = null
) //03

data class G82Block(
    val typeFlag: HoneywellTypeFlag, //01
    val invoiceNumber: String, //02
    val receiverDunsNumber: String? = null, //03
    val receiverLocationNumber: String? = null, //04
    val supplierDunsNumber: String? = null, //05
    val supplierLocationNumber: String? = null, //06
    val deliveryReturnDate: Long? = null, //07 YYYYMMDD
    val ownershipTransferDate: Long? = null, //08 YYYYMMDD
    val purchaseOrderNumber: String? = null, //09
    val purchaseOrderDate: Long? = null, //10 YYYYMMDD
    val shipmentMethodOfPayment: String? = null, //11
    val codMethodOfPayment: String? = null //12
)

data class N9Block(
    val referenceNumberQualifier: HoneywellReferenceNumberQualifier, //01
    val referenceNumber: String?, //02
    val freeFormDescription: String?, //03
    val date: Long?, //04 YYYYMMDD
    val time: Long?, //05 hhmmdd
    val timeCode: String? //06
)

data class G72Block(
    val allowanceOrChargeCode: Int, //01
    val handlingMethod: HoneywellHandlingMethodCodes, //02
    val allowanceOrChargeNumber: String?, //03
    val exceptionNumber: String?, //04
    val allowanceOrChargeRate: Float?, //05
    val allowanceOrChargeQty: Float?, //06
    val unitOfMeasure: String?, //07
    val allowanceOrChargeTotalAmount: Int?, //08
    val allowanceOrCHargePercent: Float?, //09
    val dollarBasisForPercent: Float?, //10
    val optionNumber: String? //11
)

data class G84Block(
    val quantity: Float?, //01
    val totalInvoiceAmount: Float?, //02
    val totalDepositDollarAmount: Float?, //03
)

data class G86Block(
    val electronicSignature: String?, //01
    val name: String? //02
)

data class G87Block(
    val initiatorCode: HoneywellInitiatorCode, //01
    val creditDebitFlag: HoneywellTypeFlag, //02 must have same value than 8201
    val supplierDeliveryOrReturnNumber: String?, //03
    val integrityCheck: String?, //04
    val adjustmentNumber: Int?, //05
    val receiverDeliveryOrReturnNumber: String? //06
)

data class G85Block(val integrityCheck: String?) //01

data class G89Block(
    val dsdSequenceNumber: String?, //01 must have same value as 8301, but when added by retailer, same as 8901 received
    val quantity: Float?, //02
    val unitOfMeasure: String?, //03
    val upc: String?, //04
    val productIdQualifier: String?, //05
    val productId: String?, //06
    val caseCode: String?, //07
    val itemListCost: Float?, //08
    val pack: Int?, //09
    val innerPack: Int?, //10
    val caseIdQualifier: String?, //11
    val caseId: String? //12
)

enum class HoneywellInitiatorCode(value: String) {
    SUPPLIER("S"),
    RETAILER("R")
}

enum class HoneywellHandlingMethodCodes(val value: String) {
    OFF_INVOICE("02"),
    INFORMATION_ONLY("15"),
    NOT_PROCESSED("12")
}

enum class HoneywellReferenceNumberQualifier(val value: String) {
    QUEBEC_OR_CGS("4O"),
    PROVINCIAL_TIN("4G")
}

enum class HoneywellTypeFlag(val value: String) {
    DEBIT("D"),
    CREDIT("C")
}

/*{
    "transaction" :
    {
        "invoices" :
        {
            "80010000080037000001" :
            {
                "G82" :
                {
                    "01" : "D",
                    "02" : "80010000080037000001",
                    "07" : "20200525"
                },
                "G84" :
                {
                    "01" : 75.0,
                    "02" : "222.84"
                },
                "ST" :
                {
                    "01" : "894"
                }
                "adjustments" :
                {
                    "1" :
                    {
                        "01" : "501",
                        "02" : "02",
                        "08" : 83.57
                    }
                },
                "items" :
                {
                    "1" :
                    {
                        "02" : "40",
                        "04" : "072220000689",
                        "08" : "1.77",
                        "10" : "FRZ PREM WHITE",
                        "MATNO" : "000000070001",
                        "TYPE" : "00",
                        "adjustments" :
                        {
                            "1" :
                            {
                                "01" : "97",
                                "02" : "02",
                                "05" : -0.023
                            }
                        }
                    },
                    "2" :
                    {
                        "02" : "25",
                        "04" : "072220000146",
                        "08" : "1.98",
                        "10" : "FRZ CR WHT RDTP",
                        "MATNO" : "000000070004",
                        "TYPE" : "00",
                        "adjustments" :
                        {
                            "1" :
                            {
                                "01" : "97",
                                "02" : "02",
                                "05" : -0.0256
                            }
                        }
                    },
                    "3" :
                    {
                        "02" : "10",
                        "04" : "072220000153",
                        "08" : "2.08",
                        "10" : "FRZ WH GR RD TP",
                        "MATNO" : "000000070005",
                        "TYPE" : "00",
                        "adjustments" :
                        {
                            "1" :
                            {
                                "01" : "97",
                                "02" : "02",
                                "05" : -0.027
                            }
                        }
                    }
                }
            }
        }
    }
}*/
class HoneywellDexRequest {
}