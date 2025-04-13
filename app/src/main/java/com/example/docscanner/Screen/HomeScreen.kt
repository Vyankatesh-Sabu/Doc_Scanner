package com.example.docscanner.Screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.docscanner.AuthViewModel
import com.example.docscanner.HomeScreen
import com.example.docscanner.SignIn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope

@Composable
fun HomeScreen(authViewModel: AuthViewModel, navController: NavController){

    val isSignedIn by authViewModel.isSignedIn
    LaunchedEffect(isSignedIn) {
        if (!isSignedIn) {
            navController.navigate(SignIn) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Welcome to Home Screen")
        Button(onClick = {
                authViewModel.signOut()
        }){
            Text("Sign Out")
        }


    }
}