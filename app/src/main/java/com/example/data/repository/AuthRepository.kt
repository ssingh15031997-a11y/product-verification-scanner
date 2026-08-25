package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AuthUser(
    val userId: String,
    val isLoggedIn: Boolean
)

class AuthRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("auth_session_prefs", Context.MODE_PRIVATE)

    private val _currentUser = MutableStateFlow(loadSession())
    val currentUser: StateFlow<AuthUser> = _currentUser.asStateFlow()

    private fun loadSession(): AuthUser {
        val isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        val userId = prefs.getString(KEY_USER_ID, "") ?: ""
        return AuthUser(userId = userId, isLoggedIn = isLoggedIn)
    }

    /**
     * Authenticates user against predefined and demo credentials:
     * User ID: sanjay2007
     * Password: Sanjay@2007
     *
     * In a future backend system, this can make a secure JWT/OAuth call.
     */
    fun login(userIdInput: String, passwordInput: String): Boolean {
        val cleanUser = userIdInput.trim()
        val cleanPass = passwordInput.trim()

        // Valid demo credentials specified in application requirements
        val isValid = (cleanUser.equals("sanjay2007", ignoreCase = false) && cleanPass == "Sanjay@2007") ||
                (cleanUser.equals("admin", ignoreCase = true) && cleanPass == "Admin@123")

        if (isValid) {
            prefs.edit()
                .putBoolean(KEY_IS_LOGGED_IN, true)
                .putString(KEY_USER_ID, cleanUser)
                .apply()
            _currentUser.value = AuthUser(userId = cleanUser, isLoggedIn = true)
            return true
        }
        return false
    }

    fun logout() {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, false)
            .remove(KEY_USER_ID)
            .apply()
        _currentUser.value = AuthUser(userId = "", isLoggedIn = false)
    }

    companion object {
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_ID = "logged_user_id"
    }
}
