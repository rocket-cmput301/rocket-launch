package com.example.rl_v1.fake

import com.example.rl_v1.model.Campsite
import com.example.rl_v1.repository.CampsiteRepository
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/** [CampsiteRepository] backed by [FakeBackend]. */
class FakeCampsiteRepository : CampsiteRepository {

    override suspend fun publishCampsite(campsite: Campsite): Campsite {
        val saved = campsite.copy(id = UUID.randomUUID().toString())
        FakeBackend.campsites.update { it + saved }
        return saved
    }

    override fun observeAllCampsites(): Flow<List<Campsite>> =
        FakeBackend.campsites

    override fun observeCampsitesByOwner(ownerId: String): Flow<List<Campsite>> =
        FakeBackend.campsites.map { list -> list.filter { it.ownerId == ownerId } }
}