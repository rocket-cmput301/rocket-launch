package com.example.rl_v1.ui.theme.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rl_v1.model.Campsite
import com.example.rl_v1.model.Reservation
import com.example.rl_v1.repository.CampsiteRepository
import com.example.rl_v1.repository.FirestoreCampsiteRepository
import com.example.rl_v1.search.filterBy
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

/**
 * connects campsite screens to Firestore .
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

    private val _searchQuery = MutableStateFlow("")

    /** the text currently in the search bar. */
    val searchQuery: StateFlow<String> = _searchQuery

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }
    data class Filters(val distanceFilter: Int?=0, val dates: Pair<String, String>?=Pair("",""), val numGuests: Int?=0, val preferences: List<String>?=emptyList())
    private val _filters = MutableStateFlow(Filters())

    /** all campsites narrowed by the search text. */
    var searchedCampsites: StateFlow<List<Campsite>> =
        combine(allCampsites, _searchQuery, _filters) { list, query, filter ->
            list.search(query).filterBy(filter);
        }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val ownerCampsites = campsiteRepo
        .observeCampsitesByOwner(userId)
        .asState(emptyList())

    // placeholders until reservations are connected to Firestore.
    val ownerReservations: StateFlow<List<Reservation>> =
        MutableStateFlow(emptyList())

    val entrantReservations: StateFlow<List<Reservation>> =
        MutableStateFlow(emptyList())

    private var publishing = false
    fun setFilters(distanceFilter: Int, dates: Pair<String, String>, numGuests: Int, preferences: List<String>) {
        _filters.value = Filters(
            distanceFilter = distanceFilter,        //needs to be connected to maps to determine device -> location device
            dates = dates,                          //Pair("mm/dd/yyyy","mm/dd/yyyy")
            numGuests = numGuests,
            preferences = preferences               //corresponds to Campsite class' attribute 'geographicFeatures'
        )
    }

    /** publishes a listing owned by the current user. */
    fun publish(
        name: String,
        location: String,
        maxOccupancy: Int,
        pricePerNight: Double
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
                        pricePerNight = pricePerNight
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