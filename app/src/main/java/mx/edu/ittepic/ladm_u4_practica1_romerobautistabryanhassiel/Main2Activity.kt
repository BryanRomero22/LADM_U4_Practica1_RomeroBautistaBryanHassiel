package mx.edu.ittepic.ladm_u4_practica1_romerobautistabryanhassiel

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.annotation.SuppressLint
import android.content.Intent
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.activity_main2.*
import mx.edu.ittepic.ladm_u4_practica1_romerobautistabryanhassiel.Utils.BD
import mx.edu.ittepic.ladm_u4_practica1_romerobautistabryanhassiel.Utils.Utils

class Main2Activity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        this.title = "Contactos"
    }

    private val db = BD.getInstance(this)
    private var contactos : ArrayList<ContactsDB>? = null

    override fun onResume() {
        super.onResume()
        lista()
    }

    private fun lista() {
        contactos = obtenerContactos()
        contactos?.let { c ->
            val data = ArrayList<String>()
            c.forEach { contact ->
                val type = if (contact.type == 0) "No Deseados" else "Deseados"

                data.add("Nombre: ${contact.name}\nTelefono:\n${contact.number.substring(0, contact.number.length - 1)}\nTipo de contacto: $type")
            }
            addContactsList.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, data)
            addContactsList.setOnItemClickListener { _, _, position, _ ->
                AlertDialog.Builder(this)
                    .setTitle("Qué deseas hacer?")
                    .setMessage("${data[position]}\n")
                    .setPositiveButton("Eliminar"){_, _ ->
                        eliminarContacto(c[position])
                    }
                    .setNegativeButton("Editar"){_, _ ->
                        val editarContacto = Intent(this, Main4Activity :: class.java)
                        editarContacto.putExtra("id", c[position].id)
                        editarContacto.putExtra("nombre", c[position].name)
                        editarContacto.putExtra("numero", c[position].number)
                        editarContacto.putExtra("tipo", c[position].type)
                        startActivity(editarContacto)
                    }
                    .setNeutralButton("Cancelar"){_, _ ->}
                    .show()
            }
            return
        }
        Utils.showToastMessageLong("No hay contactosr", this)
    }

    private fun eliminarContacto(contact: ContactsDB) {
        val eliminar = db?.writableDatabase
        var sql = eliminar?.delete(BD.NUMEROS, "idContacto=?", arrayOf(contact.id.toString()))
        if (sql == 0) {
            Utils.showAlertMessage("Atención", "Vuelva a intentarlo", this)
            return
        }
        sql = eliminar?.delete(BD.CONTACTOS, "idContacto=?", arrayOf(contact.id.toString()))
        if (sql == 0) {
            Utils.showAlertMessage("Atención", "Vuelva a intentarlo", this)
            return
        }
        Utils.showToastMessageLong("Se elimicó con éxito", this)
        lista()
    }

    @SuppressLint("Recycle")
    private fun obtenerContactos() : ArrayList<ContactsDB>? {
        val accion = db?.readableDatabase
        val cursor = accion?.query(BD.CONTACTOS, arrayOf("*"), null, null, null, null, null)
        cursor?.let { c ->
            val contacts = ArrayList<ContactsDB>()
            if (c.moveToFirst()) {
                do {
                    contacts.add(ContactsDB(c.getInt(0), c.getString(1), obtenerNumeros(c.getInt(0)), c.getInt(2)))
                } while (c.moveToNext())
            }
            return contacts
        }
        return null
    }

    @SuppressLint("Recycle")
    private fun obtenerNumeros(id: Int): String {
        val accion = db?.readableDatabase
        val cursor = accion?.query(BD.NUMEROS, arrayOf("*"), "idContacto=?", arrayOf(id.toString()), null, null, null)
        cursor?.let { c ->
            var telefono = ""
            if (c.moveToFirst()) {
                do {
                    telefono += c.getString(2) + "\n"
                }while (c.moveToNext())
            }
            return telefono
        }
        return ""
    }
}
