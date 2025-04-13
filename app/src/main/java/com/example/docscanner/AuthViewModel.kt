package com.example.docscanner

import android.app.Activity
import android.content.Context
import android.provider.ContactsContract.CommonDataKinds.Email
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.core.os.registerForAllProfilingResults
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.docscanner.ui.theme.GoogleAuthClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AuthViewModel(applicationContext : Context) : ViewModel() {

    private val googleAuthClient = GoogleAuthClient(applicationContext)
    private val _isSignedIn = mutableStateOf(isSignedIn())
    val isSignedIn : State<Boolean> = _isSignedIn

    fun isSignedIn() : Boolean{
        return googleAuthClient.isSignedIn()
    }
    fun registerUser(email: String, password : String){
        viewModelScope.launch((Dispatchers.Main))
        {
            _isSignedIn.value = googleAuthClient.registerEmail(email, password)

        }
    }

   fun loginEmail(email: String, password : String) {
        viewModelScope.launch(Dispatchers.IO) {
            _isSignedIn.value = googleAuthClient.loginEmail(email, password)
        }
    }

    fun signOut(){
        viewModelScope.launch(Dispatchers.IO) {
            googleAuthClient.signOut()
            _isSignedIn.value = false
        }
    }

    fun googleSignIn(activity: Activity) {
        viewModelScope.launch(Dispatchers.IO) {
            _isSignedIn.value = googleAuthClient.signIn(activity)
        }
    }
}