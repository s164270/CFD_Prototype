package com.example.cfd_prototype

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [BarcodeInfo :: class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun barcodeInfoDao(): BarcodeInfoDao

    companion object {
        @Volatile
        private var INSTANCE : AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                ).createFromAsset("database/app_database.db").build()
                INSTANCE = instance
                return instance
            }
        }
    }
}