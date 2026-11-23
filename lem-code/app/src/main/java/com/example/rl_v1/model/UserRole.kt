package com.example.rl_v1.model

/**
 * Represents the roles a User can have in the app
 *
 */
enum class UserRole {
    /** User can view and book campsites. */
    ENTRANT,

    /** User can create and manage campsites. */
    OWNER,

    /** User manage the application */
    ADMIN
}
