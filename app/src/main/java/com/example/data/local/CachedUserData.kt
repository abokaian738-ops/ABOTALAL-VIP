package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "cached_hotspot_users")
data class CachedHotspotUser(
    @PrimaryKey val id: String,
    val routerHost: String,
    val name: String,
    val password: String = "",
    val profile: String = "default",
    val limitUptime: String = "unlimited",
    val comment: String = "",
    val disabled: Boolean = false,
    val bytesOut: Long = 0,
    val bytesIn: Long = 0
)

@Entity(tableName = "cached_usermanager_users")
data class CachedUserManagerUser(
    @PrimaryKey val username: String,
    val routerHost: String,
    val password: String = "",
    val profile: String = "default",
    val active: Boolean = true,
    val uptimeUsed: String = "0s",
    val downloadLimit: String = "unlimited"
)

@Entity(tableName = "cached_router_stats")
data class CachedRouterStats(
    @PrimaryKey val routerHost: String,
    val cpuUsage: Int = 0,
    val totalMemoryBytes: Long = 128 * 1024 * 1024L,
    val freeMemoryBytes: Long = 48 * 1024 * 1024L,
    val boardName: String = "MikroTik",
    val version: String = "(stable) 6.49.19",
    val uptime: String = "1d18h59m52s",
    val activeHotspotUsers: Int = 0,
    val activeUserManagerUsers: Int = 0,
    val totalInterfaces: Int = 5,
    val totalSales: String = "-",
    val generatedVouchers: Int = 0,
    val soldVouchers: Int = 0,
    val remainingVouchers: Int = 0,
    val lastSyncTime: Long = System.currentTimeMillis()
)

@Entity(tableName = "cached_usermanager_profiles")
data class CachedUserManagerProfile(
    @PrimaryKey val name: String,
    val routerHost: String,
    val validity: String = "30d",
    val price: Double = 0.0,
    val sharedUsers: Int = 1,
    val downloadLimit: String = "unlimited",
    val uptimeLimit: String = "unlimited",
    val templateName: String = ""
)

@Entity(tableName = "cached_hotspot_profiles")
data class CachedHotspotProfile(
    @PrimaryKey val name: String,
    val routerHost: String,
    val sharedUsers: String = "1",
    val rateLimit: String = "unlimited",
    val keepaliveTimeout: String = "2m"
)

@Dao
interface CacheDao {
    @Query("SELECT * FROM cached_hotspot_users WHERE routerHost = :host")
    suspend fun getHotspotUsers(host: String): List<CachedHotspotUser>

    @Query("SELECT * FROM cached_hotspot_users")
    suspend fun getAllHotspotUsers(): List<CachedHotspotUser>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHotspotUsers(users: List<CachedHotspotUser>)

    @Query("DELETE FROM cached_hotspot_users WHERE routerHost = :host")
    suspend fun clearHotspotUsers(host: String)

    @Query("SELECT * FROM cached_usermanager_users WHERE routerHost = :host")
    suspend fun getUserManagerUsers(host: String): List<CachedUserManagerUser>

    @Query("SELECT * FROM cached_usermanager_users")
    suspend fun getAllUserManagerUsers(): List<CachedUserManagerUser>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserManagerUsers(users: List<CachedUserManagerUser>)

    @Query("SELECT * FROM cached_usermanager_profiles")
    suspend fun getAllUserManagerProfiles(): List<CachedUserManagerProfile>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserManagerProfiles(profiles: List<CachedUserManagerProfile>)

    @Query("SELECT * FROM cached_hotspot_profiles")
    suspend fun getAllHotspotProfiles(): List<CachedHotspotProfile>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHotspotProfiles(profiles: List<CachedHotspotProfile>)

    @Query("SELECT * FROM cached_router_stats WHERE routerHost = :host LIMIT 1")
    suspend fun getStats(host: String): CachedRouterStats?

    @Query("SELECT * FROM cached_router_stats ORDER BY lastSyncTime DESC LIMIT 1")
    suspend fun getLatestStats(): CachedRouterStats?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStats(stats: CachedRouterStats)
}
