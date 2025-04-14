package com.example.docscanner.ui.theme

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import com.example.docscanner.BuildConfig.CLIENT_SERVER_ID
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException

class GoogleAuthClient(
    private val context : Context
) {
    private val tag = "google-sign-in"

    private val credentialManager = CredentialManager.create(context)
    private val firebaseAuth = Firebase.auth

    fun isSignedIn() : Boolean{
        if(firebaseAuth.currentUser != null){
            Log.d("---", "isSignedIn: true")
            return true
        }
        
        return false
    }

    fun getUsername() : String? {
        return firebaseAuth.currentUser?.displayName
    }
    suspend fun signIn(activity: Activity): Boolean {
        if (isSignedIn()) {
            return true
        }

        try {
            val result = buildCredentialRequest(activity)
            return handleSignIn(result)
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is CancellationException) throw e

            println(tag + "sinIn error: ${e.message}")
            return false
        }
    }

    private suspend fun handleSignIn(result: GetCredentialResponse): Boolean {
        val credential = result.credential

        if (
            credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {

            try {

                val tokenCredential = GoogleIdTokenCredential.createFrom(credential.data)

                println(tag + "name: ${tokenCredential.displayName}")
                println(tag + "email: ${tokenCredential.id}")
                println(tag + "image: ${tokenCredential.profilePictureUri}")

                val authCredential = GoogleAuthProvider.getCredential(
                    tokenCredential.idToken, null
                )
                val authResult = firebaseAuth.signInWithCredential(authCredential).await()

                return authResult.user != null

            } catch (e: GoogleIdTokenParsingException) {
                println(tag + "GoogleIdTokenParsingException: ${e.message}")
                return false
            }

        } else {
            println(tag + "credential is not GoogleIdTokenCredential")
            return false
        }

    }


    private suspend fun buildCredentialRequest(activity: Activity): GetCredentialResponse {
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(
                GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(
                        serverClientId = "27180209062-3kob89fl90o2sei2noshnfmoh8eo1giu.apps.googleusercontent.com"
                    )
                    .setAutoSelectEnabled(false)
                    .build()
            )
            .build()

        return credentialManager.getCredential(
            request = request, context = activity
        )
    }

    suspend fun signOut() {
        credentialManager.clearCredentialState(
            ClearCredentialStateRequest()
        )
        firebaseAuth.signOut()
    }

    suspend fun loginEmail(email : String, password : String) : Boolean{
        try{
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            return result.user != null
        }catch(e : Exception){
            Toast.makeText(context, "something went wrong", Toast.LENGTH_SHORT).show()
            return false
        }
    }

    suspend fun registerEmail(email : String, password : String) : Boolean{
        try{
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            return result.user != null
        }catch(e : Exception){
            Toast.makeText(context, "something went wrong", Toast.LENGTH_SHORT).show()
            return false
        }
    }

    suspend fun resetPassword(email: String){

            val result = firebaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task->
                    if(task.isSuccessful){
                        Toast.makeText(context, "Email Sent", Toast.LENGTH_LONG).show()
                    }

                    if(!task.isSuccessful){
                        Toast.makeText(context, "Wrong Email Field", Toast.LENGTH_LONG).show()
                    }
                }
    }
}