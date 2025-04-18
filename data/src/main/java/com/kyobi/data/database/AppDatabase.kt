package com.kyobi.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.kyobi.data.database.dao.TokenDao
import com.kyobi.data.database.entity.TokenEntity

@Database(entities = [TokenEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tokenDao(): TokenDao
}