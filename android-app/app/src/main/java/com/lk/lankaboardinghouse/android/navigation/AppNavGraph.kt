package com.lk.lankaboardinghouse.android.navigation

import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.lk.lankaboardinghouse.android.data.model.BoardingHouseResponseDto
import com.lk.lankaboardinghouse.android.data.model.UserResponse
import com.lk.lankaboardinghouse.android.data.remote.RetrofitClient
import com.lk.lankaboardinghouse.android.data.session.SessionManager
import com.lk.lankaboardinghouse.android.ui.admin.AdminDashboardScreen
import com.lk.lankaboardinghouse.android.ui.common.ListingDetailScreen
import com.lk.lankaboardinghouse.android.ui.login.LoginScreen
import com.lk.lankaboardinghouse.android.ui.owner.OwnerDashboardScreen
import com.lk.lankaboardinghouse.android.ui.register.RegisterScreen
import com.lk.lankaboardinghouse.android.ui.user.UserDashboardScreen

@Composable
fun AppNavGraph(modifier: Modifier = Modifier) {
    val navController: NavHostController = rememberNavController()
    val context = LocalContext.current

    LaunchedEffect(SessionManager.sessionExpired) {
        if (SessionManager.sessionExpired) {
            SessionManager.sessionExpired = false
            Toast.makeText(context, "Session expired. Please log in again.", Toast.LENGTH_LONG).show()
            logout(navController)
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route,
        modifier = modifier
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = { user ->
                    SessionManager.currentUser = user
                    navigateToDashboard(navController, user)
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = { user ->
                    SessionManager.currentUser = user
                    navigateToDashboard(navController, user)
                },
                onBackToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.AdminDashboard.route) {
            AdminDashboardScreen(
                fullName = SessionManager.currentUser?.fullName ?: "Admin",
                onLogout = { logout(navController) }
            )
        }

        composable(Screen.OwnerDashboard.route) {
            val user = SessionManager.currentUser
            OwnerDashboardScreen(
                ownerId = user?.id ?: 0L,
                fullName = user?.fullName ?: "Owner",
                onLogout = { logout(navController) },
                onViewDetails = { listing ->
                    // THIS FIXES THE ERROR: Pass the callback to navigate to the detail screen
                    navController.navigate(Screen.ListingDetail.createRoute(listing.id))
                }
            )
        }

        composable(Screen.UserDashboard.route) {
            UserDashboardScreen(
                fullName = SessionManager.currentUser?.fullName ?: "User",
                onLogout = { logout(navController) }
            )
        }

        // NEW: The Detail Screen Route
        composable(
            route = Screen.ListingDetail.route,
            arguments = listOf(navArgument("listingId") { type = NavType.LongType })
        ) { backStackEntry ->
            val listingId = backStackEntry.arguments?.getLong("listingId")
            var listing by remember { mutableStateOf<BoardingHouseResponseDto?>(null) }
            var isLoading by remember { mutableStateOf(true) }

            // Fetch the listing details by ID
            LaunchedEffect(listingId) {
                if (listingId != null && listingId != 0L) {
                    try {
                        // We fetch the owner's listings and find the one that matches the ID
                        val response = RetrofitClient.apiService.getOwnerListings(SessionManager.currentUser?.id ?: 0L)
                        if (response.isSuccessful) {
                            listing = response.body()?.find { it.id == listingId }
                        }
                    } catch (e: Exception) {
                        // Handle error
                    } finally {
                        isLoading = false
                    }
                }
            }

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.fillMaxSize())
            } else if (listing != null) {
                ListingDetailScreen(
                    listing = listing!!,
                    onBack = { navController.popBackStack() }
                )
            } else {
                Toast.makeText(context, "Listing not found", Toast.LENGTH_SHORT).show()
                navController.popBackStack()
            }
        }
    }
}

private fun navigateToDashboard(navController: NavHostController, user: UserResponse) {
    val destination = when (user.role) {
        "ADMIN" -> Screen.AdminDashboard.route
        "OWNER" -> Screen.OwnerDashboard.route
        "USER" -> Screen.UserDashboard.route
        else -> Screen.Login.route
    }

    navController.navigate(destination) {
        popUpTo(Screen.Login.route) { inclusive = true }
    }
}

private fun logout(navController: NavHostController) {
    SessionManager.clear()
    navController.navigate(Screen.Login.route) {
        popUpTo(0) { inclusive = true }
    }
}