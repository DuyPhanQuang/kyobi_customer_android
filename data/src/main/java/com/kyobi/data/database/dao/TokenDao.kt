package com.kyobi.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.kyobi.data.database.entity.TokenEntity

@Dao
interface TokenDao {
    @Insert
    suspend fun insert(token: TokenEntity)

    @Update
    suspend fun update(token: TokenEntity)

    @Query("SELECT * FROM tokens WHERE id = 1")
    suspend fun getToken(): TokenEntity?

    @Query("DELETE FROM tokens")
    suspend fun clear()
}