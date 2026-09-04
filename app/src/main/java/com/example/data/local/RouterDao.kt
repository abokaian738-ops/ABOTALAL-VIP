package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RouterDao {
    @Query("SELECT * FROM router_connections ORDER BY lastConnected DESC")
    fun getAllConnections(): Flow<List<RouterConnection>>

    @Query("SELECT * FROM router_connections WHERE id = :id LIMIT 1")
    suspend fun getConnectionById(id: Int): RouterConnection?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConnection(connection: RouterConnection): Long

    @Delete
    suspend fun deleteConnection(connection: RouterConnection)
}
