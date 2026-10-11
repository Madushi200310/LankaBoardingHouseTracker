package com.lk.lankaboardinghouse.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.lk.lankaboardinghouse.android.data.session.SessionManager
import com.lk.lankaboardinghouse.android.navigation.AppNavGraph
import com.lk.lankaboardinghouse.android.navigation.startDestinationFor
import com.lk.lankaboardinghouse.android.ui.theme.LankaBoardingHouseTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SessionManager.init(applicationContext)
        enableEdgeToEdge()
        setContent {
            LankaBoardingHouseTrackerTheme {
                // null = still checking for a saved session
                var startDestination by rememberSaveable { mutableStateOf<String?>(null) }

                LaunchedEffect(Unit) {
                    if (startDestination == null) {
                        SessionManager.restore()
                        startDestination = startDestinationFor(SessionManager.currentUser)
                    }
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val start = startDestination
                    if (start == null) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else {
                        AppNavGraph(
                            startDestination = start,
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}