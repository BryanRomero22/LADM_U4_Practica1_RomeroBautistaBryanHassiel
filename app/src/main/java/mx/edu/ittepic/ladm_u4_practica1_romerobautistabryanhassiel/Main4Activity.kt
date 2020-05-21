package mx.edu.ittepic.ladm_u4_practica1_romerobautistabryanhassiel

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.annotation.SuppressLint
import android.content.ContentValues
import android.database.sqlite.SQLiteException
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.activity_main4.*
import mx.edu.ittepic.ladm_u4_practica1_romerobautistabryanhassiel.Utils.BD
import mx.edu.ittepic.ladm_u4_practica1_romerobautistabryanhassiel.Utils.Utils

class Main4Activity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main4)
        this.title = "Agregar Contacto"
        inicializar()
    }

    private var tipo = 0
    private val bd = BD.getInstance(this)
    private var actualizar = false

    private fun inicializar() {
        val extra = intent.extras
        txtNombre.setText(extra?.getString("nombre"))
        txtNumero.setText(extra?.getString("numero")?.substring(0, extra.getString("numero")!!.length - 1))
        iniciarSpinner()
        val id = extra?.getInt("id")
        if (id != 0) {
            actualizar = true
            this.title = "Modificar Contacto"
            val selection = if (extra?.getInt("tipo") == 0) 2 else 1
            spinner.setSelection(selection)
        }

        btnGuaradrContactos.setOnClickListener {
            if (tipo == 0) {
                Utils.showAlertMessage("Atención", "Seleccione un tipo de mensaje", this)
                return@setOnClickListener
            }
            AlertDialog.Builder(this)
                .setTitle("Atención")
                .setMessage("Está seguro?")
                .setPositiveButton("Si"){_, _ ->
                    if (actualizar)
                        actualizarContacto(id)
                    else
                        guardarContacto()
                }
                .setNegativeButton("No"){_, _ ->}
                .show()
        }
    }

    private fun actualizarContacto(id : Int?) {
        val actualizar = bd?.writableDatabase
        val data = ContentValues()
        data.put("nombre", txtNombre.text.toString())
        when(tipo) {
            1 -> data.put("tipo", 1)
            2 -> data.put("tipo", 0)
        }
        val sql = actualizar?.update(BD.CONTACTOS, data, "idContacto=?", arrayOf(id?.toString()))
        if (sql == 0) {
            Utils.showAlertMessage("Error", "No se pudo actualizar", this)
            return
        }
        Utils.showToastMessageLong("Se actualizó correctamente", this)
        finish()
    }

    private fun guardarContacto() {
        try {
            val insertar = bd?.writableDatabase
            var data = ContentValues()
            data.put("nombre", txtNombre.text.toString())
            when(tipo) {
                1 -> data.put("tipo", 1)
                2 -> data.put("tipo", 0)
            }
            var sql = insertar?.insert(BD.CONTACTOS, "idContacto", data)
            if(sql?.toInt() == -1) {
                Utils.showAlertMessage("Error", "No se pudo insertar", this)
            }
            else {
                val telefono = txtNumero.text.toString().split("\n")
                val id = ultimoContacto()
                if (id == -1) {
                    Utils.showAlertMessage("Error", "No se pudo insertar", this)
                    return
                }
                telefono.forEach { n ->
                    data = ContentValues()
                    data.put("idContacto", id)
                    data.put("numero", n)
                    sql = insertar?.insert(BD.NUMEROS, "idNumero", data)
                    if (sql?.toInt() == -1) {
                        Utils.showAlertMessage("Error", "No se pudo insertar", this)
                        return
                    }
                }
                Utils.showToastMessageLong("Se insertó correctamente", this)
                finish()
            }
        }
        catch(e : SQLiteException) {
            Utils.showAlertMessage("Error", "No se pudo insertar", this)
        }
    }

    @SuppressLint("Recycle")
    private fun ultimoContacto() : Int {
        val accion = bd?.readableDatabase
        val cursor = accion?.query(BD.CONTACTOS, arrayOf("MAX(idContacto)"), null, null, null, null, null)
        cursor?.let { c ->
            if (c.moveToFirst()) {
                return c.getInt(0)
            }
        }
        return -1
    }

    private fun iniciarSpinner() {
        val opciones = arrayOf("Tipo de mensaje", "Deseado", "No Deseado")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, opciones)
        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)
        spinner.adapter = adapter
        spinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent:AdapterView<*>, view: View, position: Int, id: Long){
                tipo = position
            }
            override fun onNothingSelected(parent: AdapterView<*>){
                // Another interface callback
            }
        }
    }
}
