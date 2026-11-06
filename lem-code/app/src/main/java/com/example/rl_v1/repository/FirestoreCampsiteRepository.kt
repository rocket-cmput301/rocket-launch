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
        require(campsite.id.isBlank()) { "This campsite is already published" }
        require(campsite.ownerId.isNotBlank()) { "An owner is required" }
        validateDetails(campsite)
        require(campsite.rating == 0.0 && campsite.ratingCount == 0) {
            "New campsites start without ratings"
        }

        val document = campsites.document()
        val savedCampsite = campsite.copy(id = document.id)

        document.set(savedCampsite).await()
        return savedCampsite
    }

    /** updates the editable details of a listing, only if the owner matches. */
    override suspend fun updateCampsite(campsite: Campsite, ownerId: String) {
        require(campsite.id.isNotBlank()) { "A campsite id is required" }
        validateDetails(campsite)

        val document = campsites.document(campsite.id)
        val snapshot = document.get().await()

        require(snapshot.exists()) { "This campsite no longer exists" }
        require(snapshot.getString("ownerId") == ownerId) {
            "You can only edit your own campsites"
        }

        // Only editable fields are written: id, ownerId, rating and ratingCount stay as they are.
        document.update(
            mapOf(
                "name" to campsite.name,
                "location" to campsite.location,
                "maxOccupancy" to campsite.maxOccupancy,
                "pricePerNight" to campsite.pricePerNight,
                "description" to campsite.description,
                "amenities" to campsite.amenities,
                "latitude" to campsite.latitude,
                "longitude" to campsite.longitude,
                "imageUrl" to campsite.imageUrl,
                "rules" to campsite.rules,
                "cancellationPolicy" to campsite.cancellationPolicy
            )
        ).await()
    }

    private fun validateDetails(campsite: Campsite) {
        require(campsite.name.isNotBlank() && campsite.name.length <= 200) {
            "Enter a campsite name between 1 and 200 characters"
        }
        require(campsite.location.isNotBlank() && campsite.location.length <= 500) {
            "Enter a location between 1 and 500 characters"
        }
        require(campsite.maxOccupancy > 0) { "Capacity must be greater than zero" }

        require(campsite.pricePerNight.isFinite() && campsite.pricePerNight in 0.0..100000.0) {
            "Price must be between 0 and 100000"
        }

        // Optional details: never required, only checked when provided.
        require(campsite.description.length <= 2000) {
            "Description must be 2000 characters or fewer"
        }
        require(campsite.rules.length <= 2000) { "Rules must be 2000 characters or fewer" }

        require(campsite.cancellationPolicy.length <= 2000) {
            "Cancellation policy must be 2000 characters or fewer"
        }
        require(campsite.amenities.size <= 20 && campsite.amenities.all { it.length <= 100 }) {
            "Use at most 20 amenities of 100 characters each"
        }

        val lat = campsite.latitude
        val lng = campsite.longitude
        require((lat == null) == (lng == null)) {
            "Provide both latitude and longitude, or neither"
        }
        require(lat == null || lat in -90.0..90.0) { "Latitude must be between -90 and 90" }

        require(lng == null || lng in -180.0..180.0) { "Longitude must be between -180 and 180" }
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