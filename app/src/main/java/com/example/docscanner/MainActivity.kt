package com.example.docscanner

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.docscanner.Screen.HomeScreen
import com.example.docscanner.Screen.LoginPage
import com.example.docscanner.Screen.forgotPassword
import com.example.docscanner.Screen.signUp
import com.example.docscanner.ui.theme.DocScannerTheme
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
                        HomeScreen(authViewModel = authViewModel, navController, this@MainActivity)
                    }
                    composable<ForgotPassword> {
                        forgotPassword(authViewModel = authViewModel)
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

@Serializable
object ForgotPassword

