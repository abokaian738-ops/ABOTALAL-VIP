package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "router_connections")
data class RouterConnection(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val host: String,
    val port: Int,
    val username: String,
    val password: String,
    val useSsl: Boolean = false,
    val lastConnected: Long = System.currentTimeMillis()
)
