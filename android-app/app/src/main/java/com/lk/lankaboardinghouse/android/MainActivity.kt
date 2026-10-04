package com.lk.lankaboardinghouse.android

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.lk.lankaboardinghouse.android.ui.login.LoginScreen
import com.lk.lankaboardinghouse.android.ui.theme.LankaBoardingHouseTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LankaBoardingHouseTrackerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LoginScreen(
                        onLoginSuccess = { user ->
                            // Temporary: just show a Toast for now.
                            // Next step will replace this with navigation to the correct dashboard based on user.role
                            Toast.makeText(
                                this,
                                "Welcome ${user.fullName}! Role: ${user.role}",
                                Toast.LENGTH_LONG
                            ).show()
                        },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}