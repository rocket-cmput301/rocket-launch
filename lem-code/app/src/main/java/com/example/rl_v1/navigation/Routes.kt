package com.example.rl_v1.navigation

/** Names of the screens that aren't bottom tabs. Some need "/{id}" added to the end. */
object Routes {
    const val SELECT_DATES = "select_dates"                 // + "/{id}" (campsite id)
    const val RESERVATION_CONFIRMED = "reservation_confirmed" // + "/{id}" (reservation id)
    const val CAMPSITE_DETAILS = "campsite_details"         // + "/{id}" (campsite id)
    const val CAMPSITE_REGISTRATION = "campsite_registration"
    const val OWNER_CAMPSITES = "owner_campsites"
    const val OWNER_EDIT = "owner_edit"                     // + "/{id}" (campsite id)
    const val CONVERSATION = "conversation"
    const val FILTERS = "filters"
    const val CAMPSITE_SELECT = "campsite_select"
}
