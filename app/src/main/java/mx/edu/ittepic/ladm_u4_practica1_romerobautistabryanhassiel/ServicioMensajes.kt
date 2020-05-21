package mx.edu.ittepic.ladm_u4_practica1_romerobautistabryanhassiel

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.provider.CallLog
import android.telephony.SmsManager
import mx.edu.ittepic.ladm_u4_practica1_romerobautistabryanhassiel.Utils.BD
import java.util.*
import kotlin.collections.ArrayList

class ServicioMensajes:Service() {

    override fun onBind(arg0: Intent?): IBinder? {
        return null
    }

    private var minutosT : Timer? = null
    private var llamadasPerdidas = ArrayList<String>()
    private var llamadasFav = ArrayList<String>()
    private var primerTiempo = true
    private val db = BD.getInstance(this)

    override fun onCreate() {
        super.onCreate()
        minutosT = Timer()
        minutosT?.scheduleAtFixedRate(
            object : TimerTask() {
                override fun run() {
                    llamadasPerdidas()
                }
            }, 0, 1000 * 30)
    }

    private fun llamadasPerdidas() {
        val t = Thread(Runnable {
            cargarLlamadasPerd()
        })
        t.start()
    }

    @SuppressLint("Recycle")
    private fun cargarLlamadasPerd() {
        try {
            val llamaasUri = CallLog.Calls.CONTENT_URI
            val cursor = contentResolver.query(llamaasUri, null, null, null, null)
            cursor?.let { c ->
                if (c.moveToFirst()) {
                    llamadasPerdidas = ArrayList()
                    do{
                        if (c.getInt(c.getColumnIndex(CallLog.Calls.TYPE)) == CallLog.Calls.MISSED_TYPE) {
                            llamadasPerdidas.add(c.getString(c.getColumnIndex(CallLog.Calls.NUMBER)))
                        }
                    }while (c.moveToNext())
                }
                if (primerTiempo) {
                    llamadasFav = llamadasPerdidas
                    primerTiempo = false
                    return
                }
                if (llamadasPerdidas.size > llamadasFav.size) {
                    llamadasFav = llamadasPerdidas
                    enviarMensaje()
                }
            }
        }
        catch (e : SecurityException){}
    }

    private fun enviarMensaje() {
        val data = obtenerNumero()
        if (data.size != 0)
        {
            val message = obtenerMensaje(data[0])
            SmsManager.getDefault().sendTextMessage(data[1], null, message, null, null)
        }
    }

    private fun obtenerNumero() : ArrayList<String> {
        val number = llamadasFav[llamadasFav.size - 1]
        val accion = db?.readableDatabase
        val contactos = ArrayList<String>()
        val cursor = accion?.query(BD.NUMEROS, arrayOf("idContact", "number"), null, null, null, null, null)
        cursor?.let { c ->
            if (c.moveToFirst()) {
                var DBnumber = ""
                do {
                    DBnumber =
                        if (c.getString(1).contains("-")) {
                            c.getString(1).replace("-", "")
                        }
                        else {
                            c.getString(1)
                        }
                    if (DBnumber == number) {
                        contactos.add(c.getString(0)) // id
                        contactos.add(number) // number
                        return contactos
                    }
                }while (c.moveToNext())
            }
        }
        cursor?.close()
        return contactos
    }

    @SuppressLint("Recycle")
    private fun obtenerMensaje(id : String) : String {
        val accion = db?.readableDatabase
        var cursor = accion?.query(BD.CONTACTOS, arrayOf("type"), "idContact=?", arrayOf(id), null, null, null)
        cursor?.let { c ->
            var tipo = ""
            if (c.moveToFirst()) {
                tipo = c.getString(0)
                cursor = accion?.query(BD.MENSAJES, arrayOf("message"), "type=?", arrayOf(tipo), null, null, null)
                cursor?.let { cursor ->
                    if (cursor.moveToFirst()) {
                        return cursor.getString(0)
                    }
                }
            }
        }
        return ""
    }
}