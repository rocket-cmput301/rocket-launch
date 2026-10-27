package com.example.rl_v1.repository

import com.example.rl_v1.model.Campsite
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * saves and observes campsite listings in Cloud Firestore.
 * the listing updates, deletion and pagination are not implemented yet.
 */
class FirestoreCampsiteRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : CampsiteRepository {

    private val campsites = firestore.collection("campsites")

    /** saves a new listing and returns it with its generated document ID  */
    override suspend fun publishCampsite(campsite: Campsite): Campsite {
        require(campsite.id.isBlank()) {
            "This campsite is already published"
        }
        require(campsite.ownerId.isNotBlank()) {
            "An owner is required"
        }
        require(campsite.name.isNotBlank() && campsite.name.length <= 200) {
            "Enter a campsite name between 1 and 200 characters"
        }
        require(
            campsite.location.isNotBlank() &&
                    campsite.location.length <= 500
        ) {
            "Enter a location between 1 and 500 characters"
        }
        require(campsite.maxOccupancy > 0) {
            "Capacity must be greater than zero"
        }
        require(
            campsite.pricePerNight.isFinite() &&
                    campsite.pricePerNight in 0.0..100000.0
        ) {
            "Price must be between 0 and 100000"
        }
        require(campsite.rating == 0.0 && campsite.ratingCount == 0) {
            "New campsites start without ratings"
        }

        val document = campsites.document()
        val savedCampsite = campsite.copy(id = document.id)

        document.set(savedCampsite).await()
        return savedCampsite
    }

    /** observes all campsite listings. */
    override fun observeAllCampsites(): Flow<List<Campsite>> =
        observeQuery(campsites)

    /** observes listings belonging to the supplied owner. */
    override fun observeCampsitesByOwner(
        ownerId: String
    ): Flow<List<Campsite>> =
        observeQuery(campsites.whereEqualTo("ownerId", ownerId))

    private fun observeQuery(query: Query): Flow<List<Campsite>> =
        callbackFlow {
            val listener = query.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                } else if (snapshot != null) {
                    try {
                        val listings = snapshot.documents.mapNotNull { document ->
                            document.toObject(Campsite::class.java)
                                ?.copy(id = document.id)
                        }

                        trySend(listings)
                    } catch (error: Exception) {
                        close(error)
                    }
                }
            }

            awaitClose {
                listener.remove()
            }
        }
}