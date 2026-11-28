package com.example.rl_v1.model

/**
 * Provides device-based user identifiers used for session identification without credentials.
 */
interface DeviceIdProvider {
    /**
     * Generates a new UUID device identifier, persists it, and returns it.
     */
    fun createDeviceId(): String

    /**
     * Returns the persisted device identifier if present, or creates and persists a new one.
     */
    fun getDeviceId(): String

    /** Overwrites the persisted device ID (useful for switching test accounts). */
    fun setDeviceId(deviceId: String)

    /** Clears the persisted device ID so the next [getDeviceId] call generates a fresh UUID. */
    fun resetDeviceId()
}