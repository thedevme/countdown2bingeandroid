package io.designtoswiftui.countdown2binge.services.auth

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val credentialManager: CredentialManager = CredentialManager.create(context)

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    // Web client ID from google-services.json (OAuth 2.0 client ID)
    // This is automatically configured by Firebase
    private val webClientId: String by lazy {
        context.getString(
            context.resources.getIdentifier(
                "default_web_client_id",
                "string",
                context.packageName
            )
        )
    }

    init {
        // Set up auth state listener
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            val newState = if (user != null) {
                AuthState.SignedIn(
                    uid = user.uid,
                    email = user.email,
                    displayName = user.displayName
                )
            } else {
                AuthState.SignedOut
            }
            _uiState.update { it.copy(authState = newState, error = null) }
        }
    }

    /**
     * Observable flow of auth state changes.
     */
    val authStateFlow = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            val state = if (user != null) {
                AuthState.SignedIn(
                    uid = user.uid,
                    email = user.email,
                    displayName = user.displayName
                )
            } else {
                AuthState.SignedOut
            }
            trySend(state)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    /**
     * Check if user is currently signed in.
     */
    val isSignedIn: Boolean
        get() = auth.currentUser != null

    /**
     * Get the current user's ID if signed in.
     */
    val userId: String?
        get() = auth.currentUser?.uid

    /**
     * Sign in with Google using Credential Manager.
     *
     * @param activityContext The Activity context (required for Credential Manager)
     * @return Result indicating success or failure
     */
    suspend fun signInWithGoogle(activityContext: Context): Result<AuthState.SignedIn> {
        _uiState.update { it.copy(isLoading = true, error = null) }

        return try {
            // Build the Google ID option
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
                .setAutoSelectEnabled(true)
                .build()

            // Build the credential request
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            // Get credentials from Credential Manager
            val result = credentialManager.getCredential(
                request = request,
                context = activityContext
            )

            // Handle the result
            handleSignInResult(result)
        } catch (e: GetCredentialCancellationException) {
            val error = AuthError.SignInCancelled()
            _uiState.update { it.copy(isLoading = false, error = error) }
            Result.failure(error)
        } catch (e: GetCredentialException) {
            val error = AuthError.SignInFailed(e)
            _uiState.update { it.copy(isLoading = false, error = error) }
            Result.failure(error)
        } catch (e: Exception) {
            val error = AuthError.Unknown(e)
            _uiState.update { it.copy(isLoading = false, error = error) }
            Result.failure(error)
        }
    }

    private suspend fun handleSignInResult(result: GetCredentialResponse): Result<AuthState.SignedIn> {
        val credential = result.credential

        return when (credential) {
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                        firebaseAuthWithGoogle(googleIdTokenCredential.idToken)
                    } catch (e: GoogleIdTokenParsingException) {
                        val error = AuthError.SignInFailed(e)
                        _uiState.update { it.copy(isLoading = false, error = error) }
                        Result.failure(error)
                    }
                } else {
                    val error = AuthError.SignInFailed(Exception("Unexpected credential type"))
                    _uiState.update { it.copy(isLoading = false, error = error) }
                    Result.failure(error)
                }
            }
            else -> {
                val error = AuthError.SignInFailed(Exception("Unexpected credential type"))
                _uiState.update { it.copy(isLoading = false, error = error) }
                Result.failure(error)
            }
        }
    }

    private suspend fun firebaseAuthWithGoogle(idToken: String): Result<AuthState.SignedIn> {
        return try {
            val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(firebaseCredential).await()
            val user = authResult.user

            if (user != null) {
                val signedInState = AuthState.SignedIn(
                    uid = user.uid,
                    email = user.email,
                    displayName = user.displayName
                )
                _uiState.update {
                    it.copy(
                        authState = signedInState,
                        isLoading = false,
                        error = null
                    )
                }
                Result.success(signedInState)
            } else {
                val error = AuthError.SignInFailed(Exception("Firebase auth returned null user"))
                _uiState.update { it.copy(isLoading = false, error = error) }
                Result.failure(error)
            }
        } catch (e: Exception) {
            val error = AuthError.SignInFailed(e)
            _uiState.update { it.copy(isLoading = false, error = error) }
            Result.failure(error)
        }
    }

    /**
     * Sign out the current user.
     */
    suspend fun signOut(): Result<Unit> {
        _uiState.update { it.copy(isLoading = true, error = null) }

        return try {
            // Clear credential state
            credentialManager.clearCredentialState(ClearCredentialStateRequest())

            // Sign out from Firebase
            auth.signOut()

            _uiState.update {
                it.copy(
                    authState = AuthState.SignedOut,
                    isLoading = false,
                    error = null
                )
            }
            Result.success(Unit)
        } catch (e: Exception) {
            val error = AuthError.SignOutFailed(e)
            _uiState.update { it.copy(isLoading = false, error = error) }
            Result.failure(error)
        }
    }

    /**
     * Delete the user's account.
     * Note: This may require recent authentication.
     */
    suspend fun deleteAccount(): Result<Unit> {
        val user = auth.currentUser
        if (user == null) {
            val error = AuthError.NotSignedIn
            _uiState.update { it.copy(error = error) }
            return Result.failure(error)
        }

        _uiState.update { it.copy(isLoading = true, error = null) }

        return try {
            // Delete user from Firebase Auth
            user.delete().await()

            // Clear credential state
            credentialManager.clearCredentialState(ClearCredentialStateRequest())

            _uiState.update {
                it.copy(
                    authState = AuthState.SignedOut,
                    isLoading = false,
                    error = null
                )
            }
            Result.success(Unit)
        } catch (e: FirebaseAuthRecentLoginRequiredException) {
            val error = AuthError.ReauthenticationRequired
            _uiState.update { it.copy(isLoading = false, error = error) }
            Result.failure(error)
        } catch (e: Exception) {
            val error = AuthError.DeletionFailed(e)
            _uiState.update { it.copy(isLoading = false, error = error) }
            Result.failure(error)
        }
    }

    /**
     * Re-authenticate and then delete the account.
     * Use this when deleteAccount() returns ReauthenticationRequired.
     *
     * @param activityContext The Activity context for re-authentication
     */
    suspend fun reauthenticateAndDelete(activityContext: Context): Result<Unit> {
        // First sign in again to get fresh credentials
        val signInResult = signInWithGoogle(activityContext)

        return if (signInResult.isSuccess) {
            // Now try to delete again
            deleteAccount()
        } else {
            Result.failure(signInResult.exceptionOrNull() ?: AuthError.Unknown(null))
        }
    }

    /**
     * Clear any error state.
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
