package com.example.rl_v1.model

/**
 * Represents the statuses a User can have in the app.
 * @property ACTIVE User is active and can use the app.
 * @property DELETED User has been deleted and cannot use the app.
 * @property REMOVED User has been removed from the app.
 */
enum class UserStatus {
    ACTIVE,
    DELETED,
    REMOVED
}