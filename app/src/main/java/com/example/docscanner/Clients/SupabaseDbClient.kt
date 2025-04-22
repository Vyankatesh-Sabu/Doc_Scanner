package com.example.docscanner.Clients

import android.util.Log
import com.example.docscanner.ErrorClient
import com.example.docscanner.Model.user
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns

class SupabaseDbClient {

    val supabase = createSupabaseClient(
        supabaseUrl = "https://xhbrclaxqcixdjeavqnc.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InhoYnJjbGF4cWNpeGRqZWF2cW5jIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDUwMzc5MzUsImV4cCI6MjA2MDYxMzkzNX0.e-3o6Sh6RPdpgzmEY68FxBC8gAEKMtHAT96vGmpfrv0"
    ) {
        install(Postgrest)
    }

    suspend fun getUser(email : String) : Result<user>{
        return try{
            val response = supabase.from("User")
                .select(Columns.list("uid, firstName, lastName, email, documents(*)"),
                    request = {
                        filter {
                            eq("email", email)
                        }
                    })
                .decodeSingle<user>()
            Result.success(response)
        }catch (e : Exception){
            ErrorClient().error(e)
            Result.failure(e)
        }
    }

    suspend fun registerUserData(uid:String, email: String, firstName : String, lastName : String){
        try{
            val newUser = user(uid = uid, firstName = firstName, lastName = lastName, email = email, documents = emptyList())
            val result = supabase.from("User")
                .insert(newUser)
            Log.d("user Register", result.data)
        }catch (e : Exception){
            Log.d("Failure", "can't register")
        }
    }

}