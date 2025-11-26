package com.fabio.cashcontrol.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.fabio.cashcontrol.model.Transaction
import com.fabio.cashcontrol.util.Converters

@Database(
    entities = [Transaction::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cashcontrol.db"
                )
                    // .fallbackToDestructiveMigration() // use se mudar o schema e não quiser migration
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
