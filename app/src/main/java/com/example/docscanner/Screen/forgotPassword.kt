package com.example.docscanner.Screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.example.docscanner.AuthViewModel


@Composable
fun forgotPassword(authViewModel: AuthViewModel){
    var email by remember { mutableStateOf("") }
    Surface {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Forgot Password ?", fontSize = 28.sp)

            OutlinedTextField(value = email, onValueChange = {email = it}, label = { Text("Email Address") })

            Button(onClick = {
                authViewModel.resetMail(email)
            }) {
                Text("Reset Password")
            }
        }
    }
}