package com.kyobi.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.kyobi.data.database.entity.PreloadedMediaEntity
import timber.log.Timber

@Dao
interface PreloadedMediaDao {
    @Upsert
    suspend fun insert(entity: PreloadedMediaEntity)

    @Query("SELECT * FROM preloaded_media WHERE url = :url")
    suspend fun getByUrl(url: String): PreloadedMediaEntity?

    @Query("SELECT * FROM preloaded_media ORDER BY timestamp DESC LIMIT 50")
    suspend fun getAll(): List<PreloadedMediaEntity>

    @Query("DELETE FROM preloaded_media WHERE timestamp < :threshold")
    suspend fun deleteOlderThan(threshold: Long)

    @Query("DELETE FROM preloaded_media WHERE timestamp < :threshold")
    suspend fun deleteOlderThanWithLog(threshold: Long) {
        Timber.tag("PreloadedMediaDao").d("Deleting preloaded media older than $threshold")
        deleteOlderThan(threshold)
    }
}