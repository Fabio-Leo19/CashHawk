package com.fabio.cashcontrol.core

import android.content.Context
import androidx.room.Room
import com.fabio.cashcontrol.data.AppDatabase

object DatabaseProvider {

    private var db: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        if (db == null) {
            db = Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                "cashcontrol.db"
            ).build()
        }
        return db!!
    }
}