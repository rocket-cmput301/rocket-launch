package com.example.rl_v1.repository

import com.example.rl_v1.model.Campsite
import kotlinx.coroutines.flow.Flow

interface CampsiteRepository {

    // fails if the campsite is already published or if it doesn't exist
    suspend fun publishCampsite(campsite: Campsite): Campsite

    // what the entrant will see
    fun observeAllCampsites(): Flow<List<Campsite>>

    //what the owner sees
    fun observeCampsitesByOwner(ownerId: String): Flow<List<Campsite>>

    // fails if the campsite doesn't exist or isn't owned by ownerId
    suspend fun updateCampsite(campsite: Campsite, ownerId: String)
}