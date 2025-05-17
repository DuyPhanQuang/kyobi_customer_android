package com.kyobi.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.kyobi.data.database.dao.PreloadedMediaDao
import com.kyobi.data.database.converter.AppTypeConverters as AppTypeConverters
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
@TypeConverters(AppTypeConverters::class)
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
                        db.execSQL("ALTER TABLE preloaded_media ADD COLUMN id INTEGER NOT NULL DEFAULT 0")
                        db.execSQL("ALTER TABLE preloaded_media ADD COLUMN tsCacheKeys TEXT NOT NULL DEFAULT '[]'")
                    }
                }).build()
                Timber.tag("AppDatabase").d("Creating AppDatabase instance: $instance")
                INSTANCE = instance
                instance
            }
        }
    }
}