package com.example.imcc

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

/**
 * @author Ahmed Guedmioui
 */
class GoogleAuthClient(
    private val context: Context,
) {

    private val tag = "GoogleAuthClient: "
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val googleSignInClient: GoogleSignInClient

    init {
        // Configura las opciones de Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("649469196072-ikor9pb8k79o2sc58r7n446dpkqrgqtq.apps.googleusercontent.com")
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(context, gso)
    }

    // Verifica si el usuario ya está autenticado
    fun isSignedIn(): Boolean {
        if (firebaseAuth.currentUser != null) {
            println("$tag already signed in")
            return true
        }
        return false
    }

    // Obtiene el Intent para iniciar el flujo de Google Sign-In
    fun getSignInIntent(): Intent {
        return googleSignInClient.signInIntent
    }

    // Maneja el resultado del Intent de Google Sign-In
    suspend fun handleSignInResult(data: Intent?): Boolean {
        if (data == null) {
            println("$tag Sign-in intent is null")
            return false
        }

        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)
            return firebaseAuthWithGoogle(account)
        } catch (e: ApiException) {
            println("$tag Sign-in failed: ${e.statusCode} - ${e.message}")
            return false
        } catch (e: Exception) {
            println("$tag Error in handleSignInResult: ${e.message}")
            return false
        }
    }

    // Autentica con Firebase usando el token de Google
    private suspend fun firebaseAuthWithGoogle(account: GoogleSignInAccount): Boolean {
        try {
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            val user = authResult.user

            if (user != null) {
                println("$tag name: ${user.displayName}")
                println("$tag email: ${user.email}")
                println("$tag image: ${user.photoUrl}")
                return true
            }
            return false
        } catch (e: Exception) {
            println("$tag Firebase auth error: ${e.message}")
            return false
        }
    }

    // Cierra sesión
    suspend fun signOut() {
        try {
            googleSignInClient.signOut().await()
            firebaseAuth.signOut()
            println("$tag Signed out successfully")
        } catch (e: Exception) {
            println("$tag Sign-out error: ${e.message}")
        }
    }
}