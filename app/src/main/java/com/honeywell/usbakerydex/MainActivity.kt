package com.honeywell.usbakerydex

import android.app.Activity
import android.os.Bundle
import com.honeywell.usbakerydex.dex.model.DEXTransaction
import com.honeywell.usbakerydex.dex.model.DEXTransmission.Builder
import com.honeywell.usbakerydex.dex.model.HoneywellParser
import com.honeywell.usbakerydex.versatiledex.VersatileConverter
import com.honeywell.usbakerydex.versatiledex.model.VersatileDexResponse
import org.json.JSONObject

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //moveTaskToBack(true)
        finish()

        val jsonString895 = "{\"ReceiveDexData\":{\"DXS\":{\"RetailerCommunicationID\":\"1111111111\",\"FunctionalIdentifierCode\":\"DX\",\"VersionOrReleaseOrIndustryIdentifierCode\":\"004010UCS\",\"TransmissionControlNumber\":3,\"SupplierCommunicationID\":\"1111111111\",\"TestIndicator\":\"P\"},\"InvoiceList\":[{\"Invoice Status\":{\"InvoiceNumber\":\"80010000080037000002\",\"Status\":\"Adjusted\"},\"ST\":{\"TransactionSetID\":\"895\",\"TransactionSetControlNumber\":2},\"G87\":{\"InitiatorCode\":\"R\",\"CreditDebitFlag\":\"D\",\"SupplierDeliveryReturnNumber\":\"80010000080037000002\",\"IntegrityCheckValue\":\"B837\",\"AdjustmentNumber\":1},\"G89\":[{\"SequenceNumber\":1,\"Quantity\":30.0},{\"SequenceNumber\":2,\"ItemListCost\":2.22},{\"SequenceNumber\":3,\"G72\":[{\"AllowanceCode\":\"96\",\"MethodOfHandling\":\"12\",\"AllowanceNumber\":\"REMOVE\"},{\"AllowanceCode\":\"97\",\"MethodOfHandling\":\"02\",\"AllowanceRate\":\"-.02\",\"AllowanceQuantity\":\"25\",\"UOMCode\":\"EA\"}]}],\"G84\":{\"TotalQuantity\":77.0,\"TotalInvoiceAmount\":293.78},\"G86\":{\"Signature\":\"5230\"},\"G85\":{\"IntegrityCheckValue\":\"E11C\"},\"SE\":{\"SegmentCount\":13,\"TransactionSetControlNumber\":2}}],\"DXE\":{\"TransmissionControlNumber\":3,\"NumberOfTransactionSetsIncluded\":1}}}"
        val jsonString894 = "{\n" +
                "\t\"CONFIG\": \n" +
                "\t{\n" +
                "\t\t\"RETAILER\":\n" +
                "\t\t{\n" +
                "\t\t\t\"COMMUNICATIONSID\" : \"1111111111\",\n" +
                "\t\t\t\"DEXVERSION\" : \"005010UCS\",\n" +
                "\t\t\t\"DUNSNUMBER\" : \"007654329\",\n" +
                "\t\t\t\"LOCATION\" : {}\n" +
                "\t\t},\n" +
                "\t\t\"SUPPLIER\" : \n" +
                "\t\t{\n" +
                "\t\t\t\"SIGNATUREKEY\" : 123\n" +
                "\t\t},\n" +
                "\t\t\"TRANSACTIONSETCONTROLNUMBER\" : 1,\n" +
                "\t\t\"TRANSMISSIONCONTROLNUMBER\" : 1\n" +
                "\t},\n" +
                "\t\"INITIALIZATION\" : \n" +
                "\t{\n" +
                "\t\t\"COMMETHOD\" : \"BTLE\",\n" +
                "\t\t\"EVTSRCID\" : 7363,\n" +
                "\t\t\"INIFILE\" : \"//mnt/sdcard//Android//data//DSD12//config.ini\",\n" +
                "\t\t\"INSTANCENAME\" : \"DEX\",\n" +
                "\t\t\"SYNCHTYPE\" : 2\n" +
                "\t},\n" +
                "\t\"transaction\" :\n" +
                "\t{\n" +
                "\t\t\"invoices\" : \n" +
                "\t\t{\n" +
                "\t\t\t\"80010000080037000001\" : \n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"G82\" : \n" +
                "\t\t\t\t{\n" +
                "\t\t\t\t\t\"01\" : \"D\",\n" +
                "\t\t\t\t\t\"02\" : \"80010000080037000001\",\n" +
                "\t\t\t\t\t\"07\" : \"20200525\"\n" +
                "\t\t\t\t},\n" +
                "\t\t\t\t\"G84\" : \n" +
                "\t\t\t\t{\n" +
                "\t\t\t\t\t\"01\" : 75.0,\n" +
                "\t\t\t\t\t\"02\" : \"222.84\"\n" +
                "\t\t\t\t},\n" +
                "\t\t\t\t\"ST\" : \n" +
                "\t\t\t\t{\n" +
                "\t\t\t\t\t\"01\" : \"894\"\n" +
                "\t\t\t\t},\n" +
                "\t\t\t\t\"adjustments\" : \n" +
                "\t\t\t\t{\n" +
                "\t\t\t\t\t\"1\" : \n" +
                "\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\"01\" : \"501\",\n" +
                "\t\t\t\t\t\t\"02\" : \"02\",\n" +
                "\t\t\t\t\t\t\"08\" : 83.57\n" +
                "\t\t\t\t\t}\n" +
                "\t\t\t\t},\n" +
                "\t\t\t\t\"items\" : \n" +
                "\t\t\t\t{\n" +
                "\t\t\t\t\t\"1\" : \n" +
                "\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\"02\" : \"40\",\n" +
                "\t\t\t\t\t\t\"04\" : \"072220000689\",\n" +
                "\t\t\t\t\t\t\"08\" : \"1.77\",\n" +
                "\t\t\t\t\t\t\"10\" : \"FRZ PREM WHITE\",\n" +
                "\t\t\t\t\t\t\"MATNO\" : \"000000070001\",\n" +
                "\t\t\t\t\t\t\"TYPE\" : \"00\",\n" +
                "\t\t\t\t\t\t\"adjustments\" : \n" +
                "\t\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\t\"1\" : \n" +
                "\t\t\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\t\t\"01\" : \"97\",\n" +
                "\t\t\t\t\t\t\t\t\"02\" : \"02\",\n" +
                "\t\t\t\t\t\t\t\t\"05\" : -0.023\n" +
                "\t\t\t\t\t\t\t}\n" +
                "\t\t\t\t\t\t}\n" +
                "\t\t\t\t\t},\n" +
                "\t\t\t\t\t\"2\" : \n" +
                "\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\"02\" : \"25\",\n" +
                "\t\t\t\t\t\t\"04\" : \"072220000146\",\n" +
                "\t\t\t\t\t\t\"08\" : \"1.98\",\n" +
                "\t\t\t\t\t\t\"10\" : \"FRZ CR WHT RDTP\",\n" +
                "\t\t\t\t\t\t\"MATNO\" : \"000000070004\",\n" +
                "\t\t\t\t\t\t\"TYPE\" : \"00\",\n" +
                "\t\t\t\t\t\t\"adjustments\" : \n" +
                "\t\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\t\"1\" : \n" +
                "\t\t\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\t\t\"01\" : \"97\",\n" +
                "\t\t\t\t\t\t\t\t\"02\" : \"02\",\n" +
                "\t\t\t\t\t\t\t\t\"05\" : -0.0256\n" +
                "\t\t\t\t\t\t\t}\n" +
                "\t\t\t\t\t\t}\n" +
                "\t\t\t\t\t},\n" +
                "\t\t\t\t\t\"3\" : \n" +
                "\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\"02\" : \"10\",\n" +
                "\t\t\t\t\t\t\"04\" : \"072220000153\",\n" +
                "\t\t\t\t\t\t\"08\" : \"2.08\",\n" +
                "\t\t\t\t\t\t\"10\" : \"FRZ WH GR RD TP\",\n" +
                "\t\t\t\t\t\t\"MATNO\" : \"000000070005\",\n" +
                "\t\t\t\t\t\t\"TYPE\" : \"00\",\n" +
                "\t\t\t\t\t\t\"adjustments\" : \n" +
                "\t\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\t\"1\" : \n" +
                "\t\t\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\t\t\"01\" : \"97\",\n" +
                "\t\t\t\t\t\t\t\t\"02\" : \"02\",\n" +
                "\t\t\t\t\t\t\t\t\"05\" : -0.027\n" +
                "\t\t\t\t\t\t\t}\n" +
                "\t\t\t\t\t\t}\n" +
                "\t\t\t\t\t}\n" +
                "\t\t\t\t}\n" +
                "\t\t\t}\n" +
                "\t\t}\n" +
                "\t}\n" +
                "}"
        val json = JSONObject(jsonString894)

        val dexTransmission = Builder()
            .with(HoneywellParser.readConfiguration(json)!!)
            .with(HoneywellParser.readInitialization(json)!!)
            .with(DEXTransaction.readTransaction(json)!!).build()

        val versatile = dexTransmission.toVersatile()
        val versatileDexResponseString = "140701:015830 894:USR 1007 ADJ_QTY 2 1 2\n" +
                "140701:015831 894:USR 1007 ADJ_QTY 4 1 4\n" +
                "140701:015832 895:SVR 1007 ADJ_LOCATION 102 100\n" +
                "140701:015832 895:USR 1007 INVC_STATUS 3"
        val result = VersatileConverter.toVersatileDexResponse(versatileDexResponseString, 5010)
        result.lines.forEach { item -> print(item.toString()) }
        dexTransmission.merge(result)
        //then, parse to versatile doing: dex.toVersatile()
        print(versatile)
    }
}
