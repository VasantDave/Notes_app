package com.example.todo.notesappsql

import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.todo.notesappsql.databinding.ActivityAddNoteBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddNoteActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddNoteBinding
    private lateinit var dbHelper: DBHelper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        dbHelper = DBHelper(this)
        binding.backButton.setOnClickListener {
            try {
                finish()
            } catch (e: Exception) {
                Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
            }
        }
        binding.moreButton.setOnClickListener { view ->
            showMenu(view)
        }
        binding.saveNote.setOnClickListener {
            if (binding.addNoteTitle.text.toString().trim().isEmpty()) {
                binding.addNoteTitle.error = "Title Can't be blank"
            } else if (binding.addNoteDesc.text.toString().trim().isEmpty()) {
                binding.addNoteDesc.error = "Description Can't be blank"
            } else {
                try {
                    saveNotes()
                } catch (e: Exception) {
                    Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showMenu(view: View) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.menuInflater.inflate(R.menu.add_menu, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.clearAll -> {
                    try {
                        binding.addNoteTitle.text.clear()
                        binding.addNoteDesc.text.clear()
                    } catch (e: Exception) {
                        Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
                    }
                    true
                }

                else -> false
            }
        }
        popupMenu.show()
    }

    private fun saveNotes() {
        val newNotes =
            Notes(
                0,
                binding.addNoteTitle.text.toString(),
                binding.addNoteDesc.text.toString(),
                getCurrentTime()
            )
        val save = dbHelper.saveNotes(newNotes)
        if (save) {
            binding.addNoteTitle.text.clear()
            binding.addNoteDesc.text.clear()
            finish()
            Toast.makeText(this, "Note Save", Toast.LENGTH_SHORT).show()
        }
    }

    fun getCurrentTime(): String {
        return dateFormat.format(Date())
    }

    companion object {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    }

}


