package com.example.rl_v1.ui

import com.example.rl_v1.model.Campsite
import com.example.rl_v1.model.Lottery
import com.example.rl_v1.model.Reservation
import com.example.rl_v1.model.WaitlistEntry
import com.example.rl_v1.repository.CampsiteRepository
import com.example.rl_v1.repository.FirestoreLotteryRepository
import com.example.rl_v1.repository.LotteryRepository
import com.example.rl_v1.repository.ReservationRepository
import com.example.rl_v1.ui.theme.viewmodel.CampsiteViewModel
import com.google.firebase.firestore.DocumentReference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** Keeps campsites in a list instead of Firestore, so UI tests don't need the internet. */
class FakeCampsiteRepository(campsites: List<Campsite>) : CampsiteRepository {

    val campsites = MutableStateFlow(campsites)

    override suspend fun publishCampsite(campsite: Campsite): Campsite {
        val saved = campsite.copy(id = "new-campsite")
        campsites.value = campsites.value + saved
        return saved
    }

    override fun observeAllCampsites(): Flow<List<Campsite>> {
        return campsites
    }

    override fun observeCampsitesByOwner(ownerId: String): Flow<List<Campsite>> {
        return campsites
    }

    override suspend fun updateCampsite(campsite: Campsite, ownerId: String) {
    }
}

/** Keeps reservations in a list instead of Firestore, so UI tests don't need the internet. */
class FakeReservationRepository(reservations: List<Reservation>) : ReservationRepository {

    val reservations = MutableStateFlow(reservations)

    override suspend fun book(
        campsiteId: String,
        entrantId: String,
        checkInDate: String,
        checkOutDate: String,
        numberOfGuests: Int,
        totalPrice: Double
    ): Result<Reservation> {
        val reservation = Reservation(id = "new-reservation", campsiteId = campsiteId)
        return Result.success(reservation)
    }

    override fun viewEntrantReservations(entrantId: String): Flow<List<Reservation>> {
        return reservations
    }

    override fun viewOwnerReservations(ownerId: String): Flow<List<Reservation>> {
        return reservations
    }

    override suspend fun reject(reservationId: String, ownerId: String): Result<Unit> {
        val updated = mutableListOf<Reservation>()
        for (reservation in reservations.value) {
            if (reservation.id == reservationId) {
                updated.add(reservation.copy(status = "rejected"))
            } else {
                updated.add(reservation)
            }
        }
        reservations.value = updated
        return Result.success(Unit)
    }

    override suspend fun cancel(reservationId: String, entrantId: String): Result<Unit> {
        reservations.value = reservations.value.map {
            if (it.id == reservationId) it.copy(status = "cancelled") else it
        }
        return Result.success(Unit)
    }
}

class FakeLotteryRepository: LotteryRepository{
    override suspend fun publishLottery(lottery: Lottery): Lottery {
        TODO("Not yet implemented")
    }

    override suspend fun updateLottery(lottery: Lottery) {
        TODO("Not yet implemented")
    }

    override suspend fun joinWaitlist(lotteryId: String, entrantId: String) {
        TODO("Not yet implemented")
    }

    override suspend fun leaveWaitlist(lotteryId: String, entrantId: String) {
        TODO("Not yet implemented")
    }

    override fun observeEntriesByUser(userId: String): Flow<List<WaitlistEntry>> {
        TODO("Not yet implemented")
    }

    override fun observeLotteriesByUser(
        userId: String,
        type: String
    ): Flow<List<Lottery>> {
        TODO("Not yet implemented")
    }

    override fun observeAllLotteries(): Flow<List<Lottery>> {
        TODO("Not yet implemented")
    }

    override fun observeLotteriesByOwner(ownerId: String): Flow<List<Lottery>> {
        TODO("Not yet implemented")
    }

    override suspend fun observeWaitlistUsersByOwner(
        campsiteId: String,
        ownerId: String
    ): Flow<Lottery> {
        TODO("Not yet implemented")
    }

    override fun notifyWaitlistUsers(campsiteId: String, ownerId: String) {
        TODO("Not yet implemented")
    }

    override suspend fun selectWinners(lotteryId: String): Int {
        TODO("Not yet implemented")
    }

    override suspend fun updateAndCheckStatus(lotteryDocument: DocumentReference): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun getLotteryID(campID: String): String? {
        TODO("Not yet implemented")
    }

    override suspend fun declineOpportunity(lotteryID: String, entrantID: String) {
        TODO("Not yet implemented")
    }

    override suspend fun resetEntries(userID: String) {
        TODO("Not yet implemented")
    }
}

/** Two campsites used by most of the UI tests. */
val hintonPines = Campsite(
    id = "hinton",
    ownerId = "test-user",
    name = "Hinton Pines",
    location = "Hinton, AB",
    maxOccupancy = 6,
    pricePerNight = 65.0,
    rules = "Quiet hours 10 pm to 7 am",
    cancellationPolicy = "Free cancellation up to 7 days before"
)

val lakeView = Campsite(
    id = "lake",
    ownerId = "test-user",
    name = "Lake View",
    location = "Canmore, AB",
    maxOccupancy = 4,
    pricePerNight = 40.0
)

/** A reservation on Hinton Pines. */
val testReservation = Reservation(
    id = "res1",
    campsiteId = "hinton",
    ownerId = "test-user",
    entrantId = "test-user",
    checkInDate = "2027-05-07",
    checkOutDate = "2027-05-09",
    numberOfGuests = 2,
    totalPrice = 130.0
)

/** A view model that uses the fake repositories and a made up user id. */
fun fakeViewModel(
    campsites: FakeCampsiteRepository = FakeCampsiteRepository(listOf(hintonPines, lakeView)),
    reservations: FakeReservationRepository = FakeReservationRepository(emptyList()),
    lotteries: FakeLotteryRepository = FakeLotteryRepository()
): CampsiteViewModel {
    return CampsiteViewModel(campsites, reservations, lotteries)
}
