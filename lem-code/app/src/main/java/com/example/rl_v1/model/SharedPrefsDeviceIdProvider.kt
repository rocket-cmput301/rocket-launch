package com.example.rl_v1.model

import android.content.Context
import androidx.core.content.edit
import java.util.UUID

/**
* [DeviceIdProvider] implementation backed by app-private [android.content.SharedPreferences].
*
* @param context Application or activity context used to access private preferences.
*/
class SharedPrefsDeviceIdProvider(
    context: Context
) : DeviceIdProvider {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Generates a new UUID string, stores it in preferences, and returns it.
     */
    override fun createDeviceId(): String {
        val newId = UUID.randomUUID().toString()
        prefs.edit { putString(KEY_DEVICE_ID, newId) }
        return newId
    }

    /**
     * Retrieves an existing non-blank device ID from preferences, or calls [createDeviceId].
     */
    override fun getDeviceId(): String {
        val existing = prefs.getString(KEY_DEVICE_ID, null)
        if (!existing.isNullOrBlank()) {
            return existing
        }
        return createDeviceId()
    }

    /**
     * Clears the stored device ID so the next call to [getDeviceId] generates a new UUID.
     * Intended for testing first-time onboarding flows.
     */
    override fun resetDeviceId() {
        prefs.edit { remove(KEY_DEVICE_ID) }
    }

    override fun setDeviceId(deviceId: String) {
        val trimmed = deviceId.trim()
        if (trimmed.isNotEmpty()) {
            prefs.edit { putString(KEY_DEVICE_ID, trimmed) }
        }
    }


    companion object {
        private const val PREFS_NAME = "campsite_device_prefs"
        private const val KEY_DEVICE_ID = "device_id"
    }
}