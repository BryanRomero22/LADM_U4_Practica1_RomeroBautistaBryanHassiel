package mx.edu.ittepic.ladm_u4_practica1_romerobautistabryanhassiel

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.annotation.SuppressLint
import android.content.ContentValues
import android.database.sqlite.SQLiteException
import kotlinx.android.synthetic.main.activity_main3.*
import mx.edu.ittepic.ladm_u4_practica1_romerobautistabryanhassiel.Utils.BD
import mx.edu.ittepic.ladm_u4_practica1_romerobautistabryanhassiel.Utils.Utils

class Main3Activity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main3)
        this.title = "Escribir Mensajes"
        accion = obtenerMensajes()
        btnGuardar.setOnClickListener {
            guardarMensajes()
        }
    }

    private var accion : Boolean = false
    private val bd = BD.getInstance(this)

    override fun onDestroy() {
        bd?.close()
        super.onDestroy()
    }

    private fun guardarMensajes() {
        if (accion) {
            actualizarContactos()
            return
        }
        InsertarContactos()
    }

    @SuppressLint("Recycle")
    private fun obtenerMensajes() : Boolean {
        try {
            val accion = bd?.readableDatabase
            val col = arrayOf("*")
            val cursor = accion?.query(BD.MENSAJES, col, null, null, null, null, null)
            cursor?.let { c ->
                if(c.moveToFirst()) {
                    val messages = ArrayList<String>()
                    do {
                        messages.add(c.getString(1))
                    }while(c.moveToNext())
                    txtDeseados.setText(messages[0])
                    txtNoDeseados.setText(messages[1])
                    return true
                }
            }
        }
        catch (e : SQLiteException){}
        return false
    }

    private fun actualizarContactos() {
        try {
            val actualizar = bd?.writableDatabase
            val dataGood = ContentValues()
            dataGood.put("mensaje", txtDeseados.text.toString())
            dataGood.put("tipo", 1)

            var sql = actualizar?.update(BD.MENSAJES, dataGood, "tipo = 1", null)

            if(sql == 0) {
                Utils.showAlertMessage("Error", "Vuelva a intentarlo", this)
            }
            else {
                val dataBad = ContentValues()
                dataBad.put("mensaje", txtNoDeseados.text.toString())
                dataBad.put("tipo", 0)
                sql = actualizar?.update(BD.MENSAJES, dataBad, "tipo = 0", null)
                if(sql == 0) {
                    Utils.showAlertMessage("Error", "Vuelva a intentarlo", this)
                    return
                }
                Utils.showToastMessageLong("Se actualizó correctamente", this)
                finish()
            }
        }
        catch(e : SQLiteException) {
            Utils.showAlertMessage("Error", "Vuelva a intentarlo", this)
        }
    }

    private fun InsertarContactos() {
        try {
            val insertar = bd?.writableDatabase
            val dataGood = ContentValues()
            dataGood.put("mensaje", txtDeseados.text.toString())
            dataGood.put("tipo", 1)
            var answer = insertar?.insert(BD.MENSAJES, "idMensajee", dataGood)
            if(answer?.toInt() == -1) {
                Utils.showAlertMessage("Error", "Vuelva a intentarlo", this)
            }
            else {
                val dataBad = ContentValues()
                dataBad.put("mensaje", txtNoDeseados.text.toString())
                dataBad.put("tipo", 0)
                answer = insertar?.insert(BD.MENSAJES, "idMensajee", dataBad)
                if(answer?.toInt() == -1) {
                    Utils.showAlertMessage("Error", "Vuelva a intentarlo", this)
                    return
                }
                Utils.showToastMessageLong("Se insertó correctamente", this)
                finish()
            }
        }
        catch(e : SQLiteException) {
            Utils.showAlertMessage("Error", "Vuelva a intentarlo", this)
        }
    }
}
