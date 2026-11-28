package com.example.rl_v1.model

/**
 * Represents the roles a User can have in the app.
 * @property ENTRANT User can view and book campsites.
 * @property OWNER User can create and manage campsites.
 * @property ADMIN User manages the application.
 */
enum class UserRole {
    ENTRANT,
    OWNER,
    ADMIN
}
