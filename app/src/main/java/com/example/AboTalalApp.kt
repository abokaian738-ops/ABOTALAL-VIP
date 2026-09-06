package com.example

import android.app.Application
import android.util.Log

class AboTalalApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Global uncaught exception handler to prevent sudden crashes and ensure stability
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("AboTalalApp", "Caught global uncaught exception on thread ${thread.name}: ${throwable.message}", throwable)
            // If it's a non-fatal coroutine or background network exception, log and recover gracefully
            try {
                if (thread.name.contains("DefaultDispatcher") || 
                    thread.name.contains("OkHttp") || 
                    thread.name.contains("IO") ||
                    throwable is java.io.IOException ||
                    throwable is java.net.SocketException ||
                    throwable is NumberFormatException) {
                    Log.w("AboTalalApp", "Suppressing fatal process death for recoverable background exception.")
                    return@setDefaultUncaughtExceptionHandler
                }
            } catch (e: Exception) {
                Log.e("AboTalalApp", "Error in global handler", e)
            }
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }
}
