package com.lk.lankaboardinghouse.android.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object AdminDashboard : Screen("admin_dashboard")
    object OwnerDashboard : Screen("owner_dashboard")
    object UserDashboard : Screen("user_dashboard")

    // NEW: The route takes the listing ID as an argument
    object ListingDetail : Screen("listing_detail/{listingId}") {
        fun createRoute(listingId: Long) = "listing_detail/$listingId"
    }
}