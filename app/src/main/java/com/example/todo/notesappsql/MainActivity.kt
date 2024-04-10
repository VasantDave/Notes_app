package com.example.todo.notesappsql

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.todo.notesappsql.databinding.ActivityMainBinding
import java.util.Calendar

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var dbHelper: DBHelper
    private lateinit var notesAdapter: NotesAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DBHelper(this)
        notesAdapter = NotesAdapter(this, dbHelper.viewAllNotes(), dbHelper)
        val noteId = intent.getIntExtra("Note_Id", -1)
        if (noteId == -1) {
            try {
                checkTouch().attachToRecyclerView(binding.homeRecyclerView)
            } catch (e: Exception) {
                Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
            }
        } else {
            val intent = Intent(this, UpdateNotesActivity::class.java)
            intent.putExtra("id", noteId)
            startActivity(intent)
        }
        binding.addNote.setOnClickListener {
            try {
                startActivity(Intent(this, AddNoteActivity::class.java))
            } catch (e: Exception) {
                Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun checkTouch(): ItemTouchHelper {
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.START or ItemTouchHelper.END,
            ItemTouchHelper.LEFT
        ) {
            val background = ColorDrawable(Color.RED)
            val deleteIcon: Drawable? =
                ContextCompat.getDrawable(applicationContext, R.drawable.delete_white)

            override fun onChildDraw(
                canvas: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                super.onChildDraw(
                    canvas,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
                val view = viewHolder.itemView
                val itemHeight = view.height
                background.setBounds(
                    view.right,
                    view.top,
                    view.right + dX.toInt(),
                    view.bottom
                )
                background.draw(canvas)
                val iconWidth = deleteIcon!!.intrinsicWidth
                val iconHeight = deleteIcon.intrinsicHeight
                val iconMargin = (itemHeight - iconHeight) / 2
                val iconTop = view.top + (itemHeight - iconHeight) / 2
                val iconBottom = iconTop + iconHeight
                val iconLeft = view.right - iconMargin - iconWidth
                val iconRight = view.right - iconMargin

                deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom)

                deleteIcon.draw(canvas)
            }

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val fromPos = viewHolder.adapterPosition
                val targetPos = target.adapterPosition
                Log.d("MOVE :", "$fromPos to $targetPos")
                notesAdapter.moveNotes(fromPos, targetPos)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                Log.d("MOVE :", position.toString())
                val notes = notesAdapter.getNotesPosition(position)
                try {
                    deleteNotes(notes)
                } catch (e: Exception) {
                    Log.d("Error", e.message.toString())
                }
            }
        })
        return itemTouchHelper
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun deleteNotes(notes: Notes) {
        AlertDialog.Builder(this)
            .setMessage("Are you sure you want to delete this Note ?")
            .setPositiveButton("YES") { _, _ ->
                dbHelper.deleteNote(notes.id)
                Toast.makeText(this, "Note Deleted", Toast.LENGTH_SHORT).show()
                refreshNotes()
            }
            .setNegativeButton("NO") { box, _ ->
                notesAdapter.notifyDataSetChanged()
                box.dismiss()
            }
            .setCancelable(false)
            .show()
    }

    private fun refreshNotes() {
        if (dbHelper.getTotalNotes() == 0) {
            binding.homeRecyclerView.visibility = View.GONE
            binding.emptyNotesImage.visibility = View.VISIBLE
            binding.totalNotes.visibility = View.GONE
            binding.notesText.visibility = View.GONE
        } else {
            binding.totalNotes.text = dbHelper.getTotalNotes().toString()
            binding.emptyNotesImage.visibility = View.GONE
            binding.homeRecyclerView.visibility = View.VISIBLE
            binding.totalNotes.visibility = View.VISIBLE
            binding.notesText.visibility = View.VISIBLE
            notesAdapter.refreshNotes(dbHelper.viewAllNotes())
            Log.d("Time Data:-", dbHelper.viewAllNotes().toString())
            binding.homeRecyclerView.layoutManager =
                StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            binding.homeRecyclerView.adapter = notesAdapter
        }
    }

    override fun onResume() {
        super.onResume()
        val calendar = Calendar.getInstance()
        Log.d("Time Main:-", calendar.time.toString())
        refreshNotes()
    }
}