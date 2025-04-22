package com.example.docscanner.Model

data class user(
    val uid : String,
    val firstName : String,
    val lastName : String,
    val email : String,
    val documents : List<ScannedDoc>
)
