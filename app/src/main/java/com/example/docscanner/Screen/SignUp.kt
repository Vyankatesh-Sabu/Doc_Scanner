package com.example.docscanner.Screen

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.docscanner.AuthViewModel
import com.example.docscanner.R
import com.example.docscanner.SignIn
import com.example.docscanner.ui.theme.GoogleAuthClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun signUp(authViewModel: AuthViewModel,navController: NavController) {
    var email :String by rememberSaveable { mutableStateOf("") }
    var pass : String by rememberSaveable { mutableStateOf("") }
    var confirmPass :String by rememberSaveable  { mutableStateOf("") }
    var isError : Boolean by rememberSaveable { mutableStateOf(false) }
    val isSignedIn by authViewModel.isSignedIn
    val activity = LocalActivity.current
    LaunchedEffect(isSignedIn) {
        if (isSignedIn) {
            navController.navigate(com.example.docscanner.HomeScreen) {
                popUpTo(SignIn) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Image(painterResource(R.drawable.sign_up), "sign up image", modifier = Modifier.size(300.dp,300.dp))

        Text("Welcome", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)

        Text("Login to your Account", color = MaterialTheme.colorScheme.onBackground)

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(value = email, onValueChange = {
            email = it
        }, label = {
            Text("Email address")
        })

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(value = pass, onValueChange = {
            pass = it
        }, label = {
            Text("Password")
        }, visualTransformation = PasswordVisualTransformation(),
            supportingText = {
                if(isError) Text("Password Mismatch")
            })

        OutlinedTextField(value = confirmPass, onValueChange = {
            confirmPass = it
        }, label = {
            Text("Confirm Password")
        }, visualTransformation = PasswordVisualTransformation(),
            supportingText = {
                if(isError) Text("Password Mismatch")
            })

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = {
                if(confirmPass == pass){
                     authViewModel.registerUser(email,pass)
                }
                else isError = true
            }, colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF1E3A8A)
            )
        ) {
            Text("Login", color = MaterialTheme.colorScheme.onPrimary)
        }


        Spacer(Modifier.height(8.dp))

        Text("or sign in with", color = MaterialTheme.colorScheme.onBackground)

        Spacer(Modifier.height(8.dp))

        Row {
            Image(painter = painterResource(R.drawable.android_light_google_sign_in), null, modifier = Modifier.clickable { activity?.let {
                authViewModel.googleSignIn(it)
            }})

        }
    }
}
