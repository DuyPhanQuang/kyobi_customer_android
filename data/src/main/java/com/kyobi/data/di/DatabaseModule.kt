package com.kyobi.data.di

import android.content.Context
import android.content.SharedPreferences
import com.kyobi.data.database.AppDatabase
import com.kyobi.data.database.dao.PreloadedMediaDao
import com.kyobi.data.database.dao.TokenDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

const val databaseName = "kyobi_database"
const val sharePrefsName = "kyobi_prefs"

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun provideTokenDao(database: AppDatabase): TokenDao {
        return database.tokenDao()
    }

    @Provides
    @Singleton
    fun providePreloadedMediaDao(database: AppDatabase): PreloadedMediaDao {
        return database.preloadedMediaDao()
    }

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences(sharePrefsName, Context.MODE_PRIVATE)
    }
}