package com.musicplayer

import android.app.Application
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.musicplayer.database.MusicDatabase
import com.musicplayer.utils.PermissionHelper
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MusicPlayerApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        try {
            // Initialize database
            initializeDatabase()
            
            // Request permissions if needed
            requestPermissionsIfNeeded()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun initializeDatabase() {
        try {
            val database = MusicDatabase.getDatabase(this)
            // Database is initialized and ready to use
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun requestPermissionsIfNeeded() {
        try {
            if (!PermissionHelper.hasReadStoragePermission(this)) {
                // Permissions will be requested in MainActivity
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}