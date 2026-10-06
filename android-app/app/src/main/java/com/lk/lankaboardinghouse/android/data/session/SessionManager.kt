package com.lk.lankaboardinghouse.android.data.session

import com.lk.lankaboardinghouse.android.data.model.UserResponse

object SessionManager {
    var currentUser: UserResponse? = null

    fun clear() {
        currentUser = null
    }
}