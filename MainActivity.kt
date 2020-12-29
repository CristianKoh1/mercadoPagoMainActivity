package com.example.netium

import android.app.Activity
import android.content.Intent
import com.mercadopago.android.px.core.MercadoPagoCheckout
import com.mercadopago.android.px.model.Payment
import androidx.annotation.NonNull
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
class MainActivity: FlutterActivity() {
    private val REQUEST_CODE=1;

    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        initFlutterChannels();
    }
    private fun initFlutterChannels(){
        val channelMercadoPago=MethodChannel(flutterEngine?.dartExecutor?.binaryMessenger, "netium.com/mercadoPago")
        channelMercadoPago.setMethodCallHandler { methodCall, result ->
            val args=methodCall.arguments as HashMap<String, Any>;
            var publicKey=args["publicKey"] as String
            var preferenceId=args["preferenceId"] as String;

            when(methodCall.method){
                "mercadoPago" -> mercadoPago(publicKey, preferenceId, result)
                "mercadoPago" -> mercadoPago(publicKey, preferenceId, result)
                else-> return@setMethodCallHandler
            }
        }
    }

    private fun mercadoPago(publicKey: String, preferenceId: String, result: MethodChannel.Result) {
        MercadoPagoCheckout.Builder(publicKey, preferenceId).build().startPayment(this@MainActivity, REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val channelMercadoPagoRespuesta = MethodChannel(flutterEngine?.dartExecutor?.binaryMessenger, "netium.com/mercadoPagoRespuesta")
        if(resultCode == MercadoPagoCheckout.PAYMENT_RESULT_CODE) {
            val payment = data!!.getSerializableExtra(MercadoPagoCheckout.EXTRA_PAYMENT_RESULT) as Payment
            val paymentStatus = payment.paymentStatus
            val paymentStatusDetails = payment.paymentStatusDetail
            val paymentID = payment.id

            val arrayList = ArrayList<String>()
            arrayList.add(paymentID.toString())
            arrayList.add(paymentStatus)
            arrayList.add(paymentStatusDetails)

            channelMercadoPagoRespuesta.invokeMethod("mercadoPagoOK", arrayList)
        } else if (resultCode == Activity.RESULT_CANCELED) {
            val arrayList = ArrayList<String>()
            arrayList.add("pagoError")
            channelMercadoPagoRespuesta.invokeMethod("mercadoPagoError", arrayList)
        } else {
            val arrayList = ArrayList<String>()
            arrayList.add("pagoCancelado")
            channelMercadoPagoRespuesta.invokeMethod("mercadoPagoError", arrayList)
        }
    }

}
