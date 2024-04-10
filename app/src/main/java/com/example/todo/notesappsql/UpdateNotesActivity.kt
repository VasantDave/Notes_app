package com.example.todo.notesappsql

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.todo.notesappsql.databinding.ActivityUpdateNotesBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class UpdateNotesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUpdateNotesBinding
    private lateinit var dbHelper: DBHelper
    private var id: Int = -1
    private var selectedCalendar: Calendar = Calendar.getInstance()
    private var selectDate = ""
    private var selectTime = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateNotesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        dbHelper = DBHelper(this)
        id = intent.getIntExtra("id", -1)
        if (id == -1) {
            finish()
        } else {
            val note = dbHelper.getSingleNote(id)
            binding.addUpdateNoteTitle.setText(note!!.title)
            binding.addUpdateNoteDesc.setText(note.description)
        }
        binding.saveUpdateNote.setOnClickListener {
            try {
                updateNote()
            } catch (e: Exception) {
                Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
            }
        }
        binding.backUpdateButton.setOnClickListener {
            try {
                finish()
            } catch (e: Exception) {
                Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
            }
        }
        binding.moreUpdateButton.setOnClickListener { view ->
            showMenu(view)
        }
    }

    private fun showMenu(view: View) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.menuInflater.inflate(R.menu.update_menu, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.delete -> {
                    try {
                        showAlertDialog()
                    } catch (e: Exception) {
                        Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
                    }
                    true
                }

                R.id.reminder -> {
                    selectDateTime()
                    true
                }

                R.id.share -> {
                    val title = binding.addUpdateNoteTitle.text.toString()
                    val desc = binding.addUpdateNoteDesc.text.toString()

                    val intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, "$title \n$desc")
                        type = "text/plain"

                    }
                    startActivity(Intent.createChooser(intent, null))
                    true
                }

                else -> false
            }
        }
        popupMenu.show()
    }

    private fun selectDateTime() {
        val currentYear = selectedCalendar.get(Calendar.YEAR)
        val currentMonth = selectedCalendar.get(Calendar.MONTH)
        val currentDay = selectedCalendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, day ->
                selectDate = ""
                if (day.toString().trim().length < 2) {
                    if ((month + 1).toString().trim().length < 2) {
                        selectDate = "0$day/0${month + 1}/$year"
                    } else {
                        selectDate = "0$day/${month + 1}/$year"
                    }
                } else {
                    if ((month + 1).toString().trim().length < 2) {
                        selectDate = "$day/0${month + 1}/$year"
                    } else {
                        selectDate = "$day/${month + 1}/$year"
                    }
                }
                selectedCalendar.set(Calendar.YEAR, year)
                selectedCalendar.set(Calendar.MONTH, month)
                selectedCalendar.set(Calendar.DAY_OF_MONTH, day)
                Log.d("Time :", selectDate)
                showTimePicker()
            },
            currentYear,
            currentMonth,
            currentDay
        )

        datePickerDialog.show()
    }

    private fun showTimePicker() {
        val currentHour = selectedCalendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = selectedCalendar.get(Calendar.MINUTE)
        val timePickerDialog = TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                selectTime = ""
                if (hourOfDay.toString().trim().length < 2) {
                    if (minute.toString().trim().length < 2) {
                        selectTime = "0$hourOfDay:0$minute"
                    } else {
                        selectTime = "0$hourOfDay:$minute"
                    }
                } else {
                    if (minute.toString().trim().length < 2) {
                        selectTime = "$hourOfDay:0$minute"
                    } else {
                        selectTime = "$hourOfDay:$minute"
                    }
                }

                selectedCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                selectedCalendar.set(Calendar.MINUTE, minute)
                selectedCalendar.set(Calendar.SECOND, 0)
                setReminder(selectedCalendar.time)
            },
            currentHour,
            currentMinute,
            true
        )
        timePickerDialog.show()
    }

    private fun setReminder(dateTime: Date) {
        val title = binding.addUpdateNoteTitle.text.toString()
        val desc = binding.addUpdateNoteDesc.text.toString()
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, ReminderBroadcastReceiver::class.java)
        intent.putExtra("Title", title)
        intent.putExtra("Desc", desc)
        intent.putExtra("id", id)
        val pendingIntent =
            PendingIntent.getBroadcast(this, id, intent, PendingIntent.FLAG_IMMUTABLE)
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, dateTime.time, pendingIntent)
        Toast.makeText(
            this,
            "Reminder Schedule for $selectDate on $selectTime ",
            Toast.LENGTH_SHORT
        ).show()
        finish()
    }

    private fun showAlertDialog() {
        AlertDialog.Builder(this)
            .setMessage("Are you sure you want to delete this Note ?")
            .setPositiveButton("YES") { _, _ ->
                dbHelper.deleteNote(id)
                Toast.makeText(this, "Note Deleted", Toast.LENGTH_SHORT).show()
                finish()
            }
            .setNegativeButton("NO") { box, _ ->
                box.dismiss()
            }
            .setCancelable(false)
            .show()
    }

    private fun updateNote() {
        dbHelper.updateNotes(
            Notes(
                id,
                binding.addUpdateNoteTitle.text.toString(),
                binding.addUpdateNoteDesc.text.toString(),
                getCurrentTime()
            )
        )
        Toast.makeText(this, "Note Updated", Toast.LENGTH_SHORT).show()
        finish()
    }

    fun getCurrentTime(): String {
        return dateFormat.format(Date())
    }

    companion object {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    }

}