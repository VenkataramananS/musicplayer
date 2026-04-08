package com.musicplayer.utils

import android.content.Context
import android.widget.Toast
import androidx.fragment.app.Fragment

object ErrorHandler {

    fun showError(context: Context, message: String) {
        try {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun showError(fragment: Fragment, message: String) {
        try {
            Toast.makeText(fragment.requireContext(), message, Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun showLongError(context: Context, message: String) {
        try {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun showLongError(fragment: Fragment, message: String) {
        try {
            Toast.makeText(fragment.requireContext(), message, Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun logError(tag: String, message: String, exception: Exception? = null) {
        try {
            android.util.Log.e(tag, message)
            exception?.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun logWarning(tag: String, message: String) {
        try {
            android.util.Log.w(tag, message)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun logInfo(tag: String, message: String) {
        try {
            android.util.Log.i(tag, message)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun logDebug(tag: String, message: String) {
        try {
            android.util.Log.d(tag, message)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getErrorMessage(exception: Exception): String {
        return when (exception) {
            is IllegalArgumentException -> "Invalid argument: ${exception.message}"
            is NullPointerException -> "Null pointer error: ${exception.message}"
            is SecurityException -> "Permission denied: ${exception.message}"
            else -> exception.message ?: "Unknown error occurred"
        }
    }
}