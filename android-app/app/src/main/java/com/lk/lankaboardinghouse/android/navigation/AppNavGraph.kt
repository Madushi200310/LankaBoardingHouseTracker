package com.lk.lankaboardinghouse.android.navigation

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.lk.lankaboardinghouse.android.data.model.UserResponse
import com.lk.lankaboardinghouse.android.data.session.SessionManager
import com.lk.lankaboardinghouse.android.ui.account.ChangePasswordScreen
import com.lk.lankaboardinghouse.android.ui.admin.AdminDashboardScreen
import com.lk.lankaboardinghouse.android.ui.login.LoginScreen
import com.lk.lankaboardinghouse.android.ui.owner.OwnerDashboardScreen
import com.lk.lankaboardinghouse.android.ui.register.RegisterScreen
import com.lk.lankaboardinghouse.android.ui.user.UserDashboardScreen

// Where the app should open: the user's dashboard if a session was restored, else Login
fun startDestinationFor(user: UserResponse?): String = when (user?.role) {
    "ADMIN" -> Screen.AdminDashboard.route
    "OWNER" -> Screen.OwnerDashboard.route
    "USER" -> Screen.UserDashboard.route
    else -> Screen.Login.route
}

// Screens that show the "Change password" footer link
private val DASHBOARD_ROUTES = setOf(
    Screen.AdminDashboard.route,
    Screen.OwnerDashboard.route,
    Screen.UserDashboard.route
)

@Composable
fun AppNavGraph(
    startDestination: String,
    modifier: Modifier = Modifier
) {
    val navController: NavHostController = rememberNavController()
    val context = LocalContext.current

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showAccountFooter = currentRoute != null && currentRoute in DASHBOARD_ROUTES

    // If the network layer flags an expired/invalid token, send the user back to login
    LaunchedEffect(SessionManager.sessionExpired) {
        if (SessionManager.sessionExpired) {
            SessionManager.sessionExpired = false
            Toast.makeText(context, "Session expired. Please log in again.", Toast.LENGTH_LONG).show()
            logout(navController)
        }
    }

    Column(modifier = modifier) {
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.weight(1f)
        ) {
            composable(Screen.Login.route) {
                LoginScreen(
                    onLoginSuccess = { user ->
                        SessionManager.saveSession(user)
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
                        SessionManager.saveSession(user)
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
                    onLogout = { logout(navController) }
                )
            }

            composable(Screen.UserDashboard.route) {
                UserDashboardScreen(
                    fullName = SessionManager.currentUser?.fullName ?: "User",
                    onLogout = { logout(navController) }
                )
            }

            composable(Screen.ChangePassword.route) {
                ChangePasswordScreen(
                    onBack = { navController.popBackStack() }
                )
            }
        }

        if (showAccountFooter) {
            TextButton(
                onClick = { navController.navigate(Screen.ChangePassword.route) },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Change password")
            }
        }
    }
}

private fun navigateToDashboard(navController: NavHostController, user: UserResponse) {
    navController.navigate(startDestinationFor(user)) {
        popUpTo(Screen.Login.route) { inclusive = true }
    }
}

private fun logout(navController: NavHostController) {
    SessionManager.clear()
    navController.navigate(Screen.Login.route) {
        popUpTo(0) { inclusive = true }
    }
}