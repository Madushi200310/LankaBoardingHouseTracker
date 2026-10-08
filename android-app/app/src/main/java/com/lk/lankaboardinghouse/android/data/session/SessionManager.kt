package com.lk.lankaboardinghouse.android.data.session

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.lk.lankaboardinghouse.android.data.model.UserResponse

object SessionManager {
    var currentUser: UserResponse? = null

    // Set to true by the network layer when the server rejects our token (expired/invalid)
    var sessionExpired by mutableStateOf(false)

    fun clear() {
        currentUser = null
    }
}