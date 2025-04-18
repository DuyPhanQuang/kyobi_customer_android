package com.kyobi.data.di

import android.content.Context
import androidx.room.Room
import com.kyobi.data.database.AppDatabase
import com.kyobi.data.database.dao.TokenDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

const val databaseName = "kyobi_database"

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            databaseName
        ).build()
    }

    @Provides
    @Singleton
    fun provideTokenDao(database: AppDatabase): TokenDao {
        return database.tokenDao()
    }
}