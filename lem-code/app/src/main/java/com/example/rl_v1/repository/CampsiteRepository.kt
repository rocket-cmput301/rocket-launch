package com.example.rl_v1.repository

import com.example.rl_v1.model.Campsite
import kotlinx.coroutines.flow.Flow

/** Reads and writes campsite listings. */
interface CampsiteRepository {

    /** Saves a new campsite and returns it with its new id. Fails if it was already published. */
    suspend fun publishCampsite(campsite: Campsite): Campsite

    /** All campsites, updated live. This is what entrants browse. */
    fun observeAllCampsites(): Flow<List<Campsite>>

    /** Only the campsites owned by [ownerId], updated live. */
    fun observeCampsitesByOwner(ownerId: String): Flow<List<Campsite>>

    /** Saves edits to a campsite. Fails if it doesn't exist or isn't owned by [ownerId]. */
    suspend fun updateCampsite(campsite: Campsite, ownerId: String)
}