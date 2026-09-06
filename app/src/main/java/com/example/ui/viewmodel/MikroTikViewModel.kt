package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.RouterConnection
import com.example.data.model.HotspotProfile
import com.example.data.model.HotspotUser
import com.example.data.model.UserManagerProfile
import com.example.data.model.UserManagerUser
import com.example.data.repository.MikroTikRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MikroTikViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = AppDatabase.getDatabase(application)
    private val routerDao = database.routerDao()
    private val logDao = database.activityLogDao()
    
    val repository = MikroTikRepository(application, logDao, database.cacheDao())

    // Theme Mode (Default is Light mode as requested: "وطبعاً اجعل الوضع الافتراضي اجعله الوضع الفاتح")
    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

    // Local saved router list (observed reactively)
    val savedConnections: StateFlow<List<RouterConnection>> = routerDao.getAllConnections()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All logs
    val activityLogs = logDao.getAllLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI exposed states from repository
    val isConnected = repository.isConnected
    val isDemoMode = repository.isDemoMode
    val currentConnection = repository.currentConnection
    val routerStats = repository.routerStats
    val hotspotUsers = repository.hotspotUsers
    val activeHotspot = repository.activeHotspot
    val hotspotProfiles = repository.hotspotProfiles
    val userManagerUsers = repository.userManagerUsers
    val userManagerProfiles = repository.userManagerProfiles
    val interfaces = repository.interfaces
    val operationStatus = repository.operationStatus

    // Local ViewModel UI States (Errors, active sub-tabs, filters)
    private val _connectionError = MutableStateFlow<String?>(null)
    val connectionError: StateFlow<String?> = _connectionError.asStateFlow()

    private val _isConnecting = MutableStateFlow(false)
    val isConnecting: StateFlow<Boolean> = _isConnecting.asStateFlow()

    fun clearConnectionError() {
        _connectionError.value = null
    }

    // Connect to real saved router
    fun connectRouter(connection: RouterConnection, onSuccess: () -> Unit) {
        _isConnecting.value = true
        _connectionError.value = null
        repository.connectToRouter(
            connection = connection,
            onSuccess = {
                _isConnecting.value = false
                // Save/update connection timestamp in DB
                viewModelScope.launch {
                    routerDao.insertConnection(connection.copy(lastConnected = System.currentTimeMillis()))
                }
                onSuccess()
            },
            onError = { errorMsg ->
                _isConnecting.value = false
                _connectionError.value = errorMsg
            }
        )
    }

    // Connect with manually input Router details
    fun connectManualRouter(name: String, host: String, port: Int, username: String, password: String, useSsl: Boolean, onSuccess: () -> Unit) {
        val conn = RouterConnection(
            name = name.ifBlank { "راوتر جديد" },
            host = host.trim(),
            port = port,
            username = username.trim(),
            password = password,
            useSsl = useSsl
        )
        
        _isConnecting.value = true
        _connectionError.value = null
        
        repository.connectToRouter(
            connection = conn,
            onSuccess = {
                _isConnecting.value = false
                // Persist to local contacts list in SQLite
                viewModelScope.launch {
                    routerDao.insertConnection(conn.copy(lastConnected = System.currentTimeMillis()))
                }
                onSuccess()
            },
            onError = { error ->
                _isConnecting.value = false
                _connectionError.value = error
            }
        )
    }

    // Launch Demo System Mode
    fun startDemoMode(onSuccess: () -> Unit) {
        _isConnecting.value = true
        _connectionError.value = null
        repository.enableDemoMode {
            _isConnecting.value = false
            onSuccess()
        }
    }

    // Disconnect
    fun logout() {
        repository.disconnect()
    }

    // Delete router from database connection history
    fun deleteSavedConnection(connection: RouterConnection) {
        viewModelScope.launch {
            routerDao.deleteConnection(connection)
        }
    }

    // Clear log histories
    fun clearLogs() {
        viewModelScope.launch {
            logDao.clearAllLogs()
        }
    }

    // Write a manual activity log event from the user interface
    fun writeActivityLog(action: String, details: String) {
        viewModelScope.launch {
            logDao.insertLog(
                com.example.data.local.ActivityLog(
                    action = action,
                    details = details,
                    routerHost = currentConnection.value?.host ?: "192.168.88.1"
                )
            )
        }
    }

    /* Hotspot Business Functions */
    fun addHotspotUser(user: HotspotUser, onSuccess: () -> Unit, onError: (String) -> Unit) {
        repository.addHotspotUser(user, onSuccess, onError)
    }

    fun deleteHotspotUser(id: String, name: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        repository.deleteHotspotUser(id, name, onSuccess, onError)
    }

    fun kickActiveUser(id: String, username: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        repository.removeActiveHotspotUser(id, username, onSuccess, onError)
    }

    fun addHotspotProfile(profile: HotspotProfile, onSuccess: () -> Unit, onError: (String) -> Unit) {
        repository.addHotspotProfile(profile, onSuccess, onError)
    }

    /* User Manager Business Functions */
    fun addUserManagerUser(user: UserManagerUser, onSuccess: () -> Unit, onError: (String) -> Unit) {
        repository.addUserManagerUser(user, onSuccess, onError)
    }

    fun deleteUserManagerUser(username: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        repository.deleteUserManagerUser(username, onSuccess, onError)
    }

    fun renewUserManagerUser(username: String, newProfile: String, resetUptime: Boolean = true, onSuccess: () -> Unit, onError: (String) -> Unit) {
        repository.renewUserManagerUser(username, newProfile, resetUptime, onSuccess, onError)
    }

    fun bulkDeleteUserManagerUsers(usernames: List<String>, onSuccess: (Int) -> Unit, onError: (String) -> Unit) {
        repository.bulkDeleteUserManagerUsers(usernames, onSuccess, onError)
    }

    fun bulkToggleUserManagerUsers(usernames: List<String>, active: Boolean, onSuccess: (Int) -> Unit) {
        repository.bulkToggleUserManagerUsers(usernames, active, onSuccess)
    }

    fun addUserManagerProfile(profile: UserManagerProfile, onSuccess: () -> Unit, onError: (String) -> Unit) {
        repository.addUserManagerProfile(profile, onSuccess, onError)
    }

    fun updateUserManagerProfile(oldName: String, profile: UserManagerProfile, onSuccess: () -> Unit, onError: (String) -> Unit) {
        repository.updateUserManagerProfile(oldName, profile, onSuccess, onError)
    }

    fun deleteUserManagerProfile(profileName: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        repository.deleteUserManagerProfile(profileName, onSuccess, onError)
    }

    fun importUserManagerProfiles(onSuccess: (Int) -> Unit, onError: (String) -> Unit) {
        repository.importUserManagerProfiles(onSuccess, onError)
    }

    fun batchAddUserManagerUsers(users: List<UserManagerUser>, batchName: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        repository.batchAddUserManagerUsers(users, batchName, onSuccess, onError)
    }

    fun cleanExpiredUserManagerUsers(onSuccess: (Int) -> Unit) {
        repository.cleanExpiredUserManagerUsers(onSuccess)
    }

    fun toggleUserManagerUser(username: String, onSuccess: () -> Unit) {
        repository.toggleUserManagerUser(username, onSuccess)
    }

    fun backupUserManagerDatabase(onSuccess: (String) -> Unit) {
        repository.backupUserManagerDatabase(onSuccess)
    }

    /* Offline Program Mode */
    fun startOfflineProgramMode(onSuccess: () -> Unit) {
        _isConnecting.value = true
        _connectionError.value = null
        repository.enableOfflineProgramMode {
            _isConnecting.value = false
            onSuccess()
        }
    }

    /* Test Connection */
    fun testConnection(host: String, port: Int, onResult: (Boolean, String) -> Unit) {
        repository.testConnection(host, port, onResult)
    }

    /* Sync Database */
    fun syncDatabase(
        onProgress: (Int, Int, String) -> Unit,
        onComplete: () -> Unit,
        onError: (String) -> Unit
    ) {
        repository.syncDatabase(onProgress, onComplete, onError)
    }

    /* Save Router Connection without connecting */
    fun saveRouterConnection(connection: RouterConnection) {
        viewModelScope.launch {
            routerDao.insertConnection(connection)
        }
    }

    /* Router System Reboot and Shutdown Control */
    fun rebootSystem(onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        repository.rebootRouter(onSuccess, onError)
    }

    fun shutdownSystem(onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        repository.shutdownRouter(onSuccess, onError)
    }

    fun refreshAllData() {
        repository.refreshData()
    }

    /* Tools & Advanced Router Operations */
    fun setHtmlDirectory(dirName: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        repository.setHtmlDirectory(dirName, onSuccess, onError)
    }

    fun toggleFreeHotspot(enableFree: Boolean, onSuccess: () -> Unit, onError: (String) -> Unit) {
        repository.toggleFreeHotspot(enableFree, onSuccess, onError)
    }

    fun toggleTtlBypass(blockSharing: Boolean, onSuccess: () -> Unit, onError: (String) -> Unit) {
        repository.toggleTtlBypass(blockSharing, onSuccess, onError)
    }

    fun toggleInterface(interfaceName: String, enable: Boolean, onSuccess: () -> Unit, onError: (String) -> Unit) {
        repository.toggleInterface(interfaceName, enable, onSuccess, onError)
    }

    fun resetInterfaceCounters(interfaceName: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        repository.resetInterfaceCounters(interfaceName, onSuccess, onError)
    }

    fun fetchPppoeUsers(onSuccess: (List<com.example.data.model.PppoeUser>) -> Unit, onError: (String) -> Unit) {
        repository.fetchPppoeUsers(onSuccess, onError)
    }

    fun addPppoeUser(user: com.example.data.model.PppoeUser, onSuccess: () -> Unit, onError: (String) -> Unit) {
        repository.addPppoeUser(user, onSuccess, onError)
    }

    fun deletePppoeUser(name: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        repository.deletePppoeUser(name, onSuccess, onError)
    }

    fun installTelegramScript(scriptName: String, scriptSource: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        repository.installTelegramScript(scriptName, scriptSource, onSuccess, onError)
    }

    fun executePing(target: String, count: Int = 4, onResult: (String) -> Unit) {
        repository.executePing(target, count, onResult)
    }

    fun fetchGenericSectionData(categoryKey: String, onSuccess: (List<com.example.data.model.RouterGenericItem>) -> Unit, onError: (String) -> Unit) {
        repository.fetchGenericSectionData(categoryKey, onSuccess, onError)
    }

    // Simple Factory pattern for ViewModel Initialization
    class Factory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MikroTikViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MikroTikViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
