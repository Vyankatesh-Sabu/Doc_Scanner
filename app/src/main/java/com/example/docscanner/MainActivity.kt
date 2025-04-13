package com.example.docscanner

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.docscanner.Screen.HomeScreen
import com.example.docscanner.Screen.LoginPage
import com.example.docscanner.Screen.signUp
import com.example.docscanner.ui.theme.DocScannerTheme
import com.example.docscanner.ui.theme.GoogleAuthClient
import com.google.android.gms.common.api.Api.Client
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlinx.serialization.*

class MainActivity : androidx.activity.ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val navController = rememberNavController()
            val authViewModel = AuthViewModel(applicationContext)
            DocScannerTheme {
                val isSignedIn = authViewModel.isSignedIn()


                NavHost(navController = navController, startDestination = if(isSignedIn) HomeScreen else SignIn) {
                    composable<SignIn> {
                        LoginPage(navController = navController, authViewModel = authViewModel)
                    }
                    composable<SignUp> {
                        signUp(authViewModel = authViewModel, navController = navController)
                    }
                    composable<HomeScreen> {
                        HomeScreen(authViewModel = authViewModel, navController)
                    }
                }
            }
        }
    }
}

@Serializable
object SignUp

@Serializable
object SignIn

@Serializable
object HomeScreen

