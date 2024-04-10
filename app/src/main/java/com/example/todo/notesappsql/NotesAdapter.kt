package com.example.todo.notesappsql

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class NotesAdapter(
    var context: Context,
    var notes: List<Notes>,
    val helper: DBHelper,
) :
    RecyclerView.Adapter<NotesAdapter.ViewHolder>() {
    lateinit var view: View

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.noteTitle)
        val desc: TextView = itemView.findViewById(R.id.noteDesc)
        val date: TextView = itemView.findViewById(R.id.noteDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        view = LayoutInflater.from(parent.context)
            .inflate(R.layout.notes_view_layout, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return notes.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val note = notes[position]
        holder.title.text = note.title
        holder.desc.text = note.description
        holder.date.text = checkTime(note.date)
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, UpdateNotesActivity::class.java)
            intent.putExtra("id", note.id)
            context.startActivity(intent)
        }
    }

    private fun checkTime(date: String): String {
        val currentDate = stringToLong(dateFormat.format(Date()))
        val createDate = stringToLong(date)
        val seconds =
            TimeUnit.MILLISECONDS.toSeconds(currentDate - createDate)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(currentDate - createDate)
        val hours = TimeUnit.MILLISECONDS.toHours(currentDate - createDate)
        val days = TimeUnit.MILLISECONDS.toDays(currentDate - createDate)
        return when {
            seconds < 60 -> "Just Now"
            minutes < 60 -> "$minutes minutes ago"
            hours < 24 -> "$hours hours ago"
            else -> "$days days ago"
        }
    }

    private fun stringToLong(dateString: String): Long {
        val date = dateFormat.parse(dateString)
        return date?.time ?: -1
    }

    fun moveNotes(fromPos: Int, targetPos: Int) {
        val movedNote = notes[fromPos]
        val mutableNotes = notes.toMutableList()
        mutableNotes.removeAt(fromPos)
        mutableNotes.add(targetPos, movedNote)
        notifyItemMoved(fromPos, targetPos)
        helper.moveNotes(fromPos, targetPos, mutableNotes)

    }

    fun getNotesPosition(position: Int): Notes {
        return notes[position]
    }

    fun refreshNotes(newNotes: List<Notes>) {
        notes = newNotes
        notifyDataSetChanged()
    }

    companion object {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    }

}