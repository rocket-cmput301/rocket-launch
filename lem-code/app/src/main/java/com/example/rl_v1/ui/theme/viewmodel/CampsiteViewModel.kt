package com.example.rl_v1.ui.theme.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rl_v1.model.Campsite
import com.example.rl_v1.model.Reservation
import com.example.rl_v1.repository.CampsiteRepository
import com.example.rl_v1.repository.FirestoreCampsiteRepository
import com.example.rl_v1.repository.FirestoreReservationRepository
import com.example.rl_v1.repository.ReservationRepository
import com.example.rl_v1.repository.stayNights
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
 * connects campsite and reservation screens to Firestore.
 * lottery eligibility is not implemented as of now
 */
class CampsiteViewModel(
    private val campsiteRepo: CampsiteRepository = FirestoreCampsiteRepository(),
    private val reservationRepo: ReservationRepository = FirestoreReservationRepository(),
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

    /** Changes the search text, which updates [searchedCampsites]. */
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    /** all campsites narrowed by the search text. */
    val searchedCampsites: StateFlow<List<Campsite>> =
        combine(allCampsites, _searchQuery) { list, query -> list.search(query) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** The filters chosen on the filter screen. The defaults mean no filter. */
    data class Filters(
        val distanceFilter: Int = 10,
        val dates: Pair<String, String>? = null,
        val numGuests: Int? = 0,
        val preferences: List<String>? = emptyList()
    )

    private val _filters = MutableStateFlow(Filters())

    /** the filters currently selected on the filter screen. */
    val filters: StateFlow<Filters> = _filters

    /** Replaces the current filters with [filters]. */
    fun setFilters(filters: Filters) {
        _filters.value = filters
    }

    /** Replaces the current filters, using a default for anything not given. */
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
    /** reservations other people made on the current user's campsites. */
    val ownerReservations: StateFlow<List<Reservation>> = reservationRepo
        .viewOwnerReservations(userId)
        .asState(emptyList())

    /** reservations the current user made. */
    val entrantReservations: StateFlow<List<Reservation>> = reservationRepo
        .viewEntrantReservations(userId)
        .asState(emptyList())

    private var publishing = false
    private var updating = false
    private var booking = false

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
        cancellationPolicy: String = "",
        availableFrom: String = "",
        availableTo: String = "",
        onDone: () -> Unit = {}
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
                        cancellationPolicy = cancellationPolicy.trim(),
                        availableFrom = availableFrom.trim(),
                        availableTo = availableTo.trim()
                    )
                )

                _message.value = "Published ${campsite.name}"
                onDone()
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

    /**
     * books [campsite] for the current user. dates are ISO strings such as "2027-05-07".
     * [onBooked] runs only when the reservation was saved.
     */
    fun book(
        campsite: Campsite,
        checkIn: String,
        checkOut: String,
        guests: Int,
        onBooked: (Reservation) -> Unit = {}
    ) {
        if (booking) return

        val nights = stayNights(checkIn, checkOut).getOrElse {
            _message.value = it.message
            return
        }
        if (guests < 1 || guests > campsite.maxOccupancy) {
            _message.value = "Guests must be between 1 and ${campsite.maxOccupancy}"
            return
        }

        booking = true
        _message.value = "Booking..."

        viewModelScope.launch {
            try {
                reservationRepo.book(
                    campsiteId = campsite.id,
                    entrantId = userId,
                    checkInDate = checkIn,
                    checkOutDate = checkOut,
                    numberOfGuests = guests,
                    totalPrice = nights.size * campsite.pricePerNight
                ).fold(
                    onSuccess = { reservation ->
                        _message.value = null
                        onBooked(reservation)
                    },
                    onFailure = { error ->
                        _message.value = "Could not book: ${error.message}"
                    }
                )
            } catch (error: CancellationException) {
                throw error
            } finally {
                booking = false
            }
        }
    }

    /** rejects a reservation on one of the current user's campsites. */
    fun reject(reservation: Reservation) {
        _message.value = "Rejecting..."
        viewModelScope.launch {
            val result = reservationRepo.reject(reservation.id, userId)
            if (result.isSuccess) {
                _message.value = "Reservation rejected"
            } else {
                _message.value = "Could not reject: ${result.exceptionOrNull()?.message}"
            }
        }
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