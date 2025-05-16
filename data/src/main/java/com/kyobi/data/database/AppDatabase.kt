package com.kyobi.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.kyobi.data.database.dao.PreloadedMediaDao
import com.kyobi.data.database.dao.TokenDao
import com.kyobi.data.database.entity.PreloadedMediaEntity
import com.kyobi.data.database.entity.TokenEntity
import com.kyobi.data.di.databaseName
import timber.log.Timber

@Database(
    entities = [
        TokenEntity::class,
        PreloadedMediaEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tokenDao(): TokenDao
    abstract fun preloadedMediaDao(): PreloadedMediaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    databaseName
                ).addMigrations(object : Migration(1, 2) {
                    override fun migrate(db: SupportSQLiteDatabase) {
                        db.execSQL("""
                            CREATE TABLE preloaded_media (
                                id INTEGER PRIMARY KEY AUTOINCREMENT,
                                url TEXT NOT NULL,
                                cacheKey TEXT NOT NULL,
                                timestamp INTEGER NOT NULL,
                                UNIQUE(url)
                            )
                        """)
                    }
                }).build()
                Timber.tag("AppDatabase").d("Creating AppDatabase instance: $instance")
                INSTANCE = instance
                instance
            }
        }
    }
}