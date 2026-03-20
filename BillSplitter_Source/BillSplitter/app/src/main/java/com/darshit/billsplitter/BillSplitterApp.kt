package com.darshit.billsplitter

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.darshit.billsplitter.data.db.AppDatabase
import com.darshit.billsplitter.data.repository.BillRepository
import com.darshit.billsplitter.utils.PreferenceManager

class BillSplitterApp : Application() {

    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
    val repository: BillRepository by lazy {
        BillRepository(database.billDao(), database.participantDao(), database.transactionDao())
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Apply saved theme
        val prefs = PreferenceManager(this)
        if (prefs.isDarkMode()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    companion object {
        lateinit var instance: BillSplitterApp
            private set
    }
}
