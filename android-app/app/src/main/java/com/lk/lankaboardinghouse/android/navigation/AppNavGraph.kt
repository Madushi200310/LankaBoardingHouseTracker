package com.lk.lankaboardinghouse.android.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lk.lankaboardinghouse.android.data.model.UserResponse
import com.lk.lankaboardinghouse.android.ui.admin.AdminDashboardScreen
import com.lk.lankaboardinghouse.android.ui.login.LoginScreen
import com.lk.lankaboardinghouse.android.ui.owner.OwnerDashboardScreen
import com.lk.lankaboardinghouse.android.ui.user.UserDashboardScreen

@Composable
fun AppNavGraph(modifier: Modifier = Modifier) {
    val navController: NavHostController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route,
        modifier = modifier
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = { user ->
                    navigateToDashboard(navController, user)
                }
            )
        }

        composable(Screen.AdminDashboard.route) {
            AdminDashboardScreen(fullName = "Admin")
        }

        composable(Screen.OwnerDashboard.route) {
            OwnerDashboardScreen(fullName = "Owner")
        }

        composable(Screen.UserDashboard.route) {
            UserDashboardScreen(fullName = "User")
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