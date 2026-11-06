package com.example.rl_v1.ui.theme.ViewModels.Owner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rl_v1.model.Campsite
import com.example.rl_v1.repository.CampsiteRepository
import com.example.rl_v1.repository.FirestoreCampsiteRepository
import com.example.rl_v1.repository.ReservationRepository
import com.google.firebase.auth.FirebaseAuth
import java.time.LocalDate
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CampsiteOwnerViewModel(
    private val reservationRepo: ReservationRepository,
    private val campsiteRepo: CampsiteRepository = FirestoreCampsiteRepository(),
    // The owner and the entrant are the same signed-in user for now.
    private val userId: String = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
) : ViewModel() {

    val allCampsites = campsiteRepo.observeAllCampsites().asState(emptyList())
    val ownerCampsites = campsiteRepo.observeCampsitesByOwner(userId).asState(emptyList())
    val ownerReservations = reservationRepo.viewOwnerReservations(userId).asState(emptyList())
    val entrantReservations = reservationRepo.viewEntrantReservations(userId).asState(emptyList())

    private val _message = MutableStateFlow<String?>(null)

    /** One-off feedback for the user, such as "Published" or a booking error. */
    val message: StateFlow<String?> = _message

    fun publish(name: String, location: String, maxOccupancy: Int, pricePerNight: Double) {
        viewModelScope.launch {
            try {
                campsiteRepo.publishCampsite(
                    Campsite(
                        ownerId = userId,
                        name = name,
                        location = location,
                        maxOccupancy = maxOccupancy,
                        pricePerNight = pricePerNight
                    )
                )
                _message.value = "Published $name"
            } catch (e: Exception) {
                _message.value = e.message ?: "Could not publish campsite"
            }
        }
    }

    fun book(campsite: Campsite, checkIn: String, checkOut: String, guests: Int) {
        val nights = try {
            ChronoUnit.DAYS.between(LocalDate.parse(checkIn), LocalDate.parse(checkOut))
        } catch (e: DateTimeParseException) {
            _message.value = "Dates must look like 2027-05-07"
            return
        }
        if (nights <= 0) {
            _message.value = "Check-out must be after check-in"
            return
        }

        viewModelScope.launch {
            val result = reservationRepo.book(
                campsiteId = campsite.id,
                entrantId = userId,
                checkInDate = checkIn,
                checkOutDate = checkOut,
                numberOfGuests = guests,
                totalPrice = nights * campsite.pricePerNight
            )
            _message.value = result.fold(
                onSuccess = { "Booked, total $${"%.2f".format(it.totalPrice)}" },
                onFailure = { it.message ?: "Booking failed" }
            )
        }
    }

    fun clearMessage() {
        _message.value = null
    }

    private fun <T> Flow<T>.asState(initial: T): StateFlow<T> =
        stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initial)
}