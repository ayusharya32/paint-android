package com.appsbyayush.paintspace.ui.settings

import android.app.Application
import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.appsbyayush.paintspace.R
import com.appsbyayush.paintspace.models.AppSettings
import com.appsbyayush.paintspace.repo.PaintRepository
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: PaintRepository,
    private val app: Application
): ViewModel() {

    companion object {
        private const val TAG = "SettingsViewModelyy"
    }

    private val _eventStateFlow = MutableStateFlow<Event>(Event.Idle)
    val events = _eventStateFlow.asStateFlow()

    var loggedInUser = repository.getAuthenticatedUser()
    var signInClient: GoogleSignInClient? = null

    private val _appSettingsStateFlow = MutableStateFlow(repository.getAppSettings())
    val appSettings = _appSettingsStateFlow.asStateFlow()

    fun getGoogleSignInOptions(): GoogleSignInOptions {
        return GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(app.applicationContext.getString(R.string.default_web_client_id))
            .requestEmail()
            .requestProfile()
            .build()
    }

    fun onSigninIntentResultRetrieved(intent: Intent?) {
        if(intent == null) {
            val exception = Exception("Some error occurred")
            sendEvent(Event.ErrorOccurred(exception))
            return
        }

        signInUserWithCredentials(intent)
    }

    fun saveCurrentSyncProcessId(processId: String) {
        repository.saveCurrentSyncProcessId(processId)
    }

    fun getCurrentSyncProcessId(): String {
        return repository.getCurrentSyncProcessId()
    }

    fun signOut() = viewModelScope.launch {
        _eventStateFlow.emit(Event.LogoutLoading)

        try {
            repository.syncDrawings()
            repository.saveAppSettings(AppSettings())
            repository.resetAllSyncTimestamps()

            repository.clearDrawingsTable()
            repository.deleteAllLocalMediaFiles()

            WorkManager.getInstance(app.applicationContext).cancelAllWork()
            repository.logoutUser()

            Log.d(TAG, "signOut: Logout Success")
            _eventStateFlow.emit(Event.LogoutSuccess)

        } catch(e: Exception) {
            Log.d(TAG, "signOut: Logout Error ${e.message}")

            _eventStateFlow.emit(Event.LogoutError(e))
        }
    }

    fun getUpdatedAppSettings(): AppSettings {
        _appSettingsStateFlow.update {
            repository.getAppSettings()
        }

        return repository.getAppSettings()
    }

    private fun signInUserWithCredentials(intent: Intent) = viewModelScope.launch {
        signInClient?.let {
            try {
                sendEvent(Event.Loading)
                val signedInAccount = GoogleSignIn.getSignedInAccountFromIntent(intent).await()
                val token = signedInAccount.idToken

                if(token != null) {
                    repository.firebaseSignInWithCredentials(token)
                    loggedInUser = repository.getAuthenticatedUser()
                    sendEvent(Event.SignInSuccess)

                } else {
                    throw Exception("Some error occurred while signing in..")
                }

            } catch(e: Exception) {
                Log.d(TAG, "signInUserWithCredentials: ${e.message}")
                sendEvent(Event.ErrorOccurred(e))
            }
        }
    }

    private fun sendEvent(event: Event) = viewModelScope.launch {
        _eventStateFlow.emit(event)
    }

    sealed class Event {
        object SignInSuccess: Event()
        object LogoutLoading: Event()
        object LogoutSuccess: Event()
        class LogoutError(val exception: Throwable): Event()
        class ErrorOccurred(val exception: Throwable): Event()
        object Loading: Event()
        object Idle : Event()
    }
}