package com.example.docscanner

import android.util.Log
import java.lang.Error

class ErrorClient() {
    fun error(error: Exception){
        error.message?.let { Log.d("Some error", it) }
    }
}