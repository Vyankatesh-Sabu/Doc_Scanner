package com.example.docscanner.Screen

import android.app.Activity
import android.content.ContentValues.TAG
import android.util.Log
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.docscanner.AuthViewModel
import com.example.docscanner.R
import com.example.docscanner.SignUp
import com.example.docscanner.HomeScreen
import com.example.docscanner.SignIn
import com.example.docscanner.ui.theme.GoogleAuthClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

@Composable
fun LoginPage(navController : NavController, authViewModel: AuthViewModel){
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    var email : String by rememberSaveable { mutableStateOf("") }
    var password : String by rememberSaveable { mutableStateOf("") }
    val isSignedIn by authViewModel.isSignedIn
    val activity = LocalActivity.current
    LaunchedEffect(isSignedIn) {
        if (isSignedIn) {
            navController.navigate(HomeScreen) {
                popUpTo(SignIn) { inclusive = true }
                launchSingleTop = true
            }
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {


        Image(painterResource(R.drawable.sign_in), "login image")
        Text("Welcome", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text("Login to your Account")
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = email, onValueChange = { email = it }, label = {
            Text("Email address")
        },
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = {
                    focusRequester.requestFocus()
                }
            )
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField( value = password, onValueChange = {
            password = it
        }, label = {
            Text("Password")
        }, visualTransformation = PasswordVisualTransformation()
            , modifier = Modifier.focusRequester(focusRequester),
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                }
            )

            )
        Spacer(Modifier.height(8.dp))
        Button(onClick = {
                    authViewModel.loginEmail(email,password)
        }, colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF1E3A8A)
        )) {
            Text("Login", color = Color.White)
        }
        Spacer(Modifier.height(16.dp))
        Row {
            Text("Forgot Password", modifier = Modifier.clickable {

            },)
            Spacer(modifier = Modifier.width(150.dp))
            Text("Sign Up", Modifier.clickable {
                navController.navigate(SignUp)
            })
        }
        Spacer(Modifier.height(8.dp))
            Text("or sign in with")
            Spacer(Modifier.height(8.dp))
            Row {
            Image(painter = painterResource(R.drawable.android_light_google_sign_in), null, modifier = Modifier.clickable { activity?.let {
                authViewModel.googleSignIn(it)
            }})
        }
    }

}
