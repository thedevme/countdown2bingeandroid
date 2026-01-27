package io.designtoswiftui.countdown2binge.services.auth

/**
 * Represents the current authentication state of the user.
 */
sealed class AuthState {
    /**
     * Initial state before auth status is determined.
     */
    data object Unknown : AuthState()

    /**
     * User is not signed in.
     */
    data object SignedOut : AuthState()

    /**
     * User is signed in.
     */
    data class SignedIn(
        val uid: String,
        val email: String?,
        val displayName: String?
    ) : AuthState()

    val isSignedIn: Boolean
        get() = this is SignedIn

    val userId: String?
        get() = (this as? SignedIn)?.uid
}

/**
 * Represents authentication errors.
 */
sealed class AuthError : Exception() {
    data class SignInFailed(override val cause: Throwable?) : AuthError() {
        override val message: String = cause?.message ?: "Sign in failed"
    }

    data class SignInCancelled(override val message: String = "Sign in was cancelled") : AuthError()

    data class SignOutFailed(override val cause: Throwable?) : AuthError() {
        override val message: String = cause?.message ?: "Sign out failed"
    }

    data object NotSignedIn : AuthError() {
        override val message: String = "User is not signed in"
    }

    data class DeletionFailed(override val cause: Throwable?) : AuthError() {
        override val message: String = cause?.message ?: "Account deletion failed"
    }

    data object ReauthenticationRequired : AuthError() {
        override val message: String = "Please sign in again to complete this action"
    }

    data class Unknown(override val cause: Throwable?) : AuthError() {
        override val message: String = cause?.message ?: "An unknown error occurred"
    }
}

/**
 * UI state for authentication operations.
 */
data class AuthUiState(
    val authState: AuthState = AuthState.Unknown,
    val isLoading: Boolean = false,
    val error: AuthError? = null
) {
    val isSignedIn: Boolean
        get() = authState.isSignedIn

    val userId: String?
        get() = authState.userId
}
