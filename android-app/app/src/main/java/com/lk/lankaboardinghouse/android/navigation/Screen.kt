package com.lk.lankaboardinghouse.android.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object AdminDashboard : Screen("admin_dashboard")
    object OwnerDashboard : Screen("owner_dashboard")
    object UserDashboard : Screen("user_dashboard")
}