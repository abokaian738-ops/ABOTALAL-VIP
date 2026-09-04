package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityLogDao {
    @Query("SELECT * FROM activity_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<ActivityLog>>

    @Query("SELECT * FROM activity_logs WHERE routerHost = :routerHost ORDER BY timestamp DESC")
    fun getLogsByRouter(routerHost: String): Flow<List<ActivityLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ActivityLog)

    @Query("DELETE FROM activity_logs WHERE routerHost = :routerHost")
    suspend fun deleteLogsByRouter(routerHost: String)

    @Query("DELETE FROM activity_logs")
    suspend fun clearAllLogs()
}
