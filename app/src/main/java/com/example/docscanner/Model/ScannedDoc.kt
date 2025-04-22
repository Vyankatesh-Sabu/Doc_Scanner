package com.example.docscanner.Model

import java.sql.Date

data class ScannedDoc(
    val fileName : String,
    val url : String,
    val createdAt : Date
)
