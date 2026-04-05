package com.solarsensear.data.repository

import com.solarsensear.data.models.UserProfile

/**
 * Handles authentication state.
 * Phase 2: Mock-only implementation. Phase 3 will integrate Firebase Auth.
 */
class AuthRepository {

    private var currentUser: UserProfile? = null

    /** Returns true if a user is currently authenticated. */
    fun isLoggedIn(): Boolean = currentUser != null

    /** Returns the current user profile. */
    fun getCurrentUser(): UserProfile? = currentUser

    /**
     * Simulates a login. Once Firebase Auth is integrated,
     * this will use GoogleSignInClient / PhoneAuthProvider.
     */
    suspend fun loginAsGuest(): Result<UserProfile> {
        val guest = UserProfile(
            uid = "guest_${System.currentTimeMillis()}",
            name = "Guest User",
            email = "",
            isGuest = true
        )
        currentUser = guest
        return Result.success(guest)
    }

    /**
     * Phase 3: Google Sign-In integration.
     * Will use the activity result to get GoogleSignInAccount,
     * then exchange for Firebase credential.
     */
    suspend fun loginWithGoogle(idToken: String): Result<UserProfile> {
        // TODO: Firebase Auth integration
        // val credential = GoogleAuthProvider.getCredential(idToken, null)
        // val authResult = FirebaseAuth.getInstance().signInWithCredential(credential).await()
        val mockUser = UserProfile(
            uid = "google_mock_${System.currentTimeMillis()}",
            name = "Siddhant",
            email = "siddhant@example.com",
            isGuest = false
        )
        currentUser = mockUser
        return Result.success(mockUser)
    }

    /**
     * Phase 3: Phone OTP integration.
     * Will use PhoneAuthProvider.verifyPhoneNumber() flow.
     */
    suspend fun loginWithPhone(phoneNumber: String): Result<UserProfile> {
        // TODO: Firebase Phone Auth integration
        val mockUser = UserProfile(
            uid = "phone_mock_${System.currentTimeMillis()}",
            name = "Phone User",
            email = "",
            isGuest = false
        )
        currentUser = mockUser
        return Result.success(mockUser)
    }

    fun logout() {
        // TODO: FirebaseAuth.getInstance().signOut()
        currentUser = null
    }
}
