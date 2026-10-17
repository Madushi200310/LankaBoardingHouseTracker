package com.lk.lankaboardinghouse.android.data.session

import android.content.Context
import android.util.Base64
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.lk.lankaboardinghouse.android.data.model.UserResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.json.JSONObject

private val Context.sessionDataStore: DataStore<Preferences> by preferencesDataStore(name = "session")

object SessionManager {

    private val KEY_ID = longPreferencesKey("id")
    private val KEY_FULL_NAME = stringPreferencesKey("full_name")
    private val KEY_EMAIL = stringPreferencesKey("email")
    private val KEY_PHONE = stringPreferencesKey("phone")
    private val KEY_ROLE = stringPreferencesKey("role")
    private val KEY_TOKEN = stringPreferencesKey("token")

    private var appContext: Context? = null

    // One writer thread, so a quick logout followed by a login is saved in the right order
    private val writeScope = CoroutineScope(SupervisorJob() + Dispatchers.IO.limitedParallelism(1))

    var currentUser: UserResponse? = null
        private set

    // Set to true by the network layer when the server rejects our token (expired/invalid)
    var sessionExpired by mutableStateOf(false)

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    // Called once at app start: loads a saved session if it is still valid
    suspend fun restore() {
        if (currentUser != null) return
        val ctx = appContext ?: return

        val prefs = ctx.sessionDataStore.data.first()
        val token = prefs[KEY_TOKEN]
        val id = prefs[KEY_ID]
        val fullName = prefs[KEY_FULL_NAME]
        val email = prefs[KEY_EMAIL]
        val phone = prefs[KEY_PHONE]
        val role = prefs[KEY_ROLE]

        if (token == null || id == null || fullName == null ||
            email == null || phone == null || role == null
        ) {
            return
        }

        if (isTokenExpired(token)) {
            ctx.sessionDataStore.edit { it.clear() }
            return
        }

        currentUser = UserResponse(id, fullName, email, phone, role, token)
    }

    fun saveSession(user: UserResponse) {
        currentUser = user
        val ctx = appContext ?: return
        writeScope.launch {
            ctx.sessionDataStore.edit { prefs ->
                prefs[KEY_ID] = user.id
                prefs[KEY_FULL_NAME] = user.fullName
                prefs[KEY_EMAIL] = user.email
                prefs[KEY_PHONE] = user.phoneNumber
                prefs[KEY_ROLE] = user.role
                prefs[KEY_TOKEN] = user.token
            }
        }
    }

    fun clear() {
        currentUser = null
        val ctx = appContext ?: return
        writeScope.launch {
            ctx.sessionDataStore.edit { it.clear() }
        }
    }

    // Reads the "exp" claim (seconds since epoch) from the JWT payload
    private fun isTokenExpired(token: String): Boolean {
        return try {
            val payload = token.split(".")[1]
            val json = String(Base64.decode(payload, Base64.URL_SAFE))
            val expiresAtSeconds = JSONObject(json).getLong("exp")
            expiresAtSeconds * 1000 <= System.currentTimeMillis()
        } catch (_: Exception) {
            true
        }
    }
}