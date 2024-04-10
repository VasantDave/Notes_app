package com.example.todo.notesappsql

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.SimpleDateFormat
import java.util.Locale

class DBHelper(context: Context) : SQLiteOpenHelper(context, DatabaseName, null, DatabaseVersion) {
    companion object {
        const val DatabaseName = "NotesDatabase"
        const val DatabaseVersion = 1
        const val table_name = "Notes"
        const val note_id = "Notes_Id"
        const val title = "Notes_Title"
        const val desc = "Notes_Desc"
        const val create_date = "Notes_Date"
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("CREATE TABLE $table_name($note_id INTEGER PRIMARY KEY, $title TEXT, $desc TEXT, $create_date TEXT)")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $table_name")
        onCreate(db)
    }

    fun saveNotes(notes: Notes): Boolean {
        val db = this.writableDatabase
        val cv = ContentValues()
        cv.put(title, notes.title)
        cv.put(desc, notes.description)
        cv.put(create_date, notes.date)
        val result = db.insert(table_name, null, cv)
        db.close()
        return result != (-1).toLong()
    }

    fun getTotalNotes(): Int {
        val db = this.readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT COUNT(*) FROM $table_name", null)
        var count = 0
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0)
        }
        cursor.close()
        db.close()
        return count
    }

    fun viewAllNotes(): List<Notes> {
        val note = mutableListOf<Notes>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $table_name", null)
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getString(0).toString().toInt()
                val title = cursor.getString(1).toString()
                val desc = cursor.getString(2).toString()
                val date = cursor.getString(3).toString()
                note.add(
                    Notes(
                        id,
                        title,
                        desc,
                        date
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return note
    }

    fun updateNotes(notes: Notes): Int {
        val db = this.writableDatabase
        val cv = ContentValues()
        cv.put(title, notes.title)
        cv.put(desc, notes.description)
        cv.put(create_date, notes.date)
        val result =
            db.update(table_name, cv, "$note_id =? ", arrayOf(notes.id.toString()))
        db.close()
        return result
    }

    fun moveNotes(fromPos: Int, newPos: Int, notes: List<Notes>) {
        val db = this.writableDatabase
        db.execSQL("DELETE FROM $table_name")
        notes.forEachIndexed { index, notes ->
            val cv = ContentValues()
            cv.put(note_id, index)
            cv.put(title, notes.title)
            cv.put(desc, notes.description)
            cv.put(create_date, notes.date)
            db.insert(table_name, null, cv)
        }
        db.close()
    }

    fun getSingleNote(id: Int): Notes? {
        val db = this.readableDatabase
        val cursor =
            db.rawQuery("SELECT * FROM $table_name WHERE $note_id = $id", null)
        return if (cursor.moveToFirst()) {
            val title = cursor.getString(1).toString()
            val desc = cursor.getString(2).toString()
            val date = cursor.getString(3).toString()
            Notes(id, title, desc, date)
        } else {
            db.close()
            return null
        }
    }

    fun deleteNote(id: Int): Int {
        val db = this.writableDatabase
        val result =
            db.delete(table_name, "$note_id = ?", arrayOf(id.toString()))
        db.close()
        return result
    }
}