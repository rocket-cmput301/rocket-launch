package com.example.rl_v1.ui.theme.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rl_v1.model.Campsite
import com.example.rl_v1.model.Reservation
import com.example.rl_v1.repository.CampsiteRepository
import com.example.rl_v1.repository.FirestoreCampsiteRepository
import com.example.rl_v1.search.search
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.example.rl_v1.search.filterBy

/**
 * connects campsite screens to Firestore.
 * reservations and lottery eligibility are not implemented as of now
 */
class CampsiteViewModel(
    private val campsiteRepo: CampsiteRepository = FirestoreCampsiteRepository(),
    private val userId: String = requireNotNull(
        FirebaseAuth.getInstance().currentUser?.uid
    ) { "sign in before creating CampsiteViewModel" }
) : ViewModel() {

    private val _message = MutableStateFlow<String?>(null)

    /** feedback shown by the screen. */
    val message: StateFlow<String?> = _message

    val allCampsites = campsiteRepo
        .observeAllCampsites()
        .asState(emptyList())

    val ownerCampsites = campsiteRepo
        .observeCampsitesByOwner(userId)
        .asState(emptyList())


    private val _searchQuery = MutableStateFlow("")

    /** the text currently in the search bar. */
    val searchQuery: StateFlow<String> = _searchQuery

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    /** all campsites narrowed by the search text. */
    val searchedCampsites: StateFlow<List<Campsite>> =
        combine(allCampsites, _searchQuery) { list, query -> list.search(query) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    data class Filters(
        val distanceFilter: Int = 10,
        val dates: Pair<String, String>? = null,
        val numGuests: Int? = 0,
        val preferences: List<String>? = emptyList()
    )

    private val _filters = MutableStateFlow(Filters())

    /** the filters currently selected on the filter screen. */
    val filters: StateFlow<Filters> = _filters

    fun setFilters(filters: Filters) {
        _filters.value = filters
    }

    fun setFilters(
        distanceFilter: Int = 10,
        dates: Pair<String, String>? = null,
        numGuests: Int = 0,
        preferences: List<String> = emptyList()
    ) {
        _filters.value = Filters(
            distanceFilter = distanceFilter,
            dates = dates,
            numGuests = numGuests,
            preferences = preferences
        )
    }

    /** all campsites narrowed by search first, then by the selected filters. */
    val filteredCampsites: StateFlow<List<Campsite>> =
        combine(searchedCampsites, _filters) { list, f -> list.filterBy(f) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    // placeholders until reservations are connected to Firestore.
    val ownerReservations: StateFlow<List<Reservation>> =
        MutableStateFlow(emptyList())

    val entrantReservations: StateFlow<List<Reservation>> =
        MutableStateFlow(emptyList())

    private var publishing = false
    private var updating = false

    /** publishes a listing owned by the current user. */
    fun publish(
        name: String,
        location: String,
        maxOccupancy: Int,
        pricePerNight: Double,
        description: String = "",
        amenities: List<String> = emptyList(),
        latitude: Double? = null,
        longitude: Double? = null,
        imageUrl: String? = null,
        rules: String = "",
        cancellationPolicy: String = ""
    ) {
        if (publishing) return

        publishing = true
        _message.value = "Publishing..."

        viewModelScope.launch {
            try {
                val campsite = campsiteRepo.publishCampsite(
                    Campsite(
                        ownerId = userId,
                        name = name.trim(),
                        location = location.trim(),
                        maxOccupancy = maxOccupancy,
                        pricePerNight = pricePerNight,
                        description = description.trim(),
                        amenities = amenities.map { it.trim() }.filter { it.isNotEmpty() },
                        latitude = latitude,
                        longitude = longitude,
                        imageUrl = imageUrl?.trim()?.ifEmpty { null },
                        rules = rules.trim(),
                        cancellationPolicy = cancellationPolicy.trim()
                    )
                )

                _message.value = "Published ${campsite.name}"
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _message.value = "Could not publish: ${error.message}"
            } finally {
                publishing = false
            }
        }
    }

    /** saves edits to one of the current user's campsites; [onDone] runs only on success. */
    fun update(campsite: Campsite, onDone: () -> Unit = {}) {
        if (updating) return

        updating = true
        _message.value = "Saving..."

        viewModelScope.launch {
            try {
                campsiteRepo.updateCampsite(campsite, userId)
                _message.value = "Saved ${campsite.name}"
                onDone()
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _message.value = "Could not save: ${error.message}"
            } finally {
                updating = false
            }
        }
    }

    /** booking is unavailable until its database support is implemented. */
    @Suppress("UNUSED_PARAMETER")
    fun book(
        campsite: Campsite,
        checkIn: String,
        checkOut: String,
        guests: Int
    ) {
        _message.value = "Reservations are not connected to Firebase yet."
    }

    /** clears the screens feedback message  */
    fun clearMessage() {
        _message.value = null
    }

    private fun <T> Flow<T>.asState(initial: T): StateFlow<T> =
        catch { error ->
            _message.value = "Could not load data: ${error.message}"
            emit(initial)
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            initial
        )
}