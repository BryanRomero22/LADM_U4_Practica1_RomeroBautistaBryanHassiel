package mx.edu.ittepic.ladm_u4_practica1_romerobautistabryanhassiel

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.ContactsContract
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_main.*
import mx.edu.ittepic.ladm_u4_practica1_romerobautistabryanhassiel.Utils.BD
import mx.edu.ittepic.ladm_u4_practica1_romerobautistabryanhassiel.Utils.Utils

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        this.title = "Contactos"
        darPermisos()
        startService(Intent(this, ServicioMensajes :: class.java))
    }

    object Constants {
        const val READ_CONTACTS_SUCCESS = 1
        const val READ_CALL_LOGS = 2
        const val SEND_SMS = 3
    }

    override fun onResume() {
        super.onResume()
        lista()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.READ_CONTACTS_SUCCESS) {
            lista()
            return
        }
        Utils.showToastMessageLong("Vuelva a intentarlo", this)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.escribirMensaje -> startActivity(Intent(this, Main3Activity :: class.java))
            R.id.mostrarContactos -> startActivity(Intent(this, Main2Activity :: class.java))
            R.id.salir -> {
                stopService(Intent(this, ServicioMensajes :: class.java))
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    @SuppressLint("Recycle")
    private fun lista() {
        val cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null)
        cursor?.let { c ->
            if (c.moveToFirst()) {
                val phones = ArrayList<Contacts>()
                do {
                    val id = c.getString(c.getColumnIndex(ContactsContract.Contacts._ID))
                    val name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                    var phone = ""
                    if (c.getInt(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                        val numbers = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", arrayOf(id), null)
                        numbers?.let { n ->
                            while (n.moveToNext()) {
                                phone += n.getString(n.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)) + "\n"
                            }
                            n.close()
                        }
                        phones.add(Contacts(name, phone))
                    }
                } while (c.moveToNext())
                mostrarLista(phones)
            }
            else {
                Utils.showToastMessageLong("No tiene contactos registrados", this)
            }
        }
    }

    private fun mostrarLista(phones : ArrayList<Contacts>) {
        val data = ArrayList<String>()
        phones.forEach { p ->
            data.add("Nombre: ${p.name}\nTelefono:\n${p.number}")
        }
        contactsList.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, data)
        contactsList.setOnItemClickListener { _, _, position, _ ->
            AlertDialog.Builder(this)
                .setTitle("Contacto")
                .setMessage("${data[position]}\nDesea agregar este contacto?")
                .setPositiveButton("SI"){_, _ ->
                    if (esValido()) {
                        val addContact = Intent(this, Main4Activity :: class.java)
                        addContact.putExtra("nombre", phones[position].name)
                        addContact.putExtra("numero", phones[position].number)

                        startActivity(addContact)
                    }
                }
                .setNegativeButton("NO"){_, _ ->}
                .show()
        }
    }

    private fun esValido() : Boolean {
        val select = BD.getInstance(this)?.readableDatabase
        val cursor = select?.query(BD.MENSAJES, arrayOf("*"), null, null, null, null, null)
        cursor?.let { c ->
            if (c.moveToFirst()) {
                c.close()
                c.close()
                return true
            }
        }

        Utils.showAlertMessage("Atención", "Debe agregar los mensajesr", this)
        return false
    }

    private fun darPermisos() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_CALL_LOG) !=
            PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_CALL_LOG), Constants.READ_CALL_LOGS)
        }
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS) !=
            PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_CONTACTS), Constants.READ_CONTACTS_SUCCESS)
        }
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.SEND_SMS) !=
            PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.SEND_SMS), Constants.SEND_SMS)
        }
    }
}
