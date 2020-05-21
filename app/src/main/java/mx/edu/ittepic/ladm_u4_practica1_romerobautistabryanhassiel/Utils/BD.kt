package mx.edu.ittepic.ladm_u4_practica1_romerobautistabryanhassiel.Utils

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class BD(context : Context) : SQLiteOpenHelper(context, "MENSAJES", null, 1) {
    companion object
    {
        const val MENSAJES = "MENSAJES"
        const val CONTACTOS = "CONTACTOS"
        const val NUMEROS = "NUMEROS"

        private var instance : BD?= null

        fun getInstance(context : Context) : BD? {
            instance.let {
                instance =
                    BD(context)
            }
            return instance
        }
    }

    override fun onCreate(db: SQLiteDatabase?) {
        // 1 -> deseado
        // 0 -> no deseado
        var sql = "CREATE TABLE MENSAJES(idMensaje INTEGER PRIMARY KEY AUTOINCREMENT,mensaje VARCHAR(2000),tipo INTEGER)"
        db?.execSQL(sql)
        sql = "CREATE TABLE CONTACTOS(idContacto INTEGER PRIMARY KEY AUTOINCREMENT,nombre VARCHAR(200),tipo INTEGER)"
        db?.execSQL(sql)
        sql = "CREATE TABLE NUMEROS(idNumero INTEGER PRIMARY KEY AUTOINCREMENT,idcontacto INTEGER,numero VARCHAR(50),FOREIGN KEY(idContacto) REFERENCES CONTACTS(idContacto))"
        db?.execSQL(sql)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS MENSAJES")
        db?.execSQL("DROP TABLE IF EXISTS CONTACTOS")
        db?.execSQL("DROP TABLE IF EXISTS NUMEROS")
        onCreate(db)
    }
}