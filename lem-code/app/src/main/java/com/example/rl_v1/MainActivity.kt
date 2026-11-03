package com.example.rl_v1

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.rl_v1.ui.theme.MainScreen
import com.example.rl_v1.ui.theme.Rlv1Theme
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

/**
 * signs the user in before opening the campsite screen.
 * anonymous account recovery after reinstall has not yet been implemented.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            Rlv1Theme {
                var signedIn by remember { mutableStateOf(false) }
                var errorMessage by remember { mutableStateOf<String?>(null) }
                var attempt by remember { mutableStateOf(0) }

                LaunchedEffect(attempt) {
                    errorMessage = null

                    try {
                        val auth = FirebaseAuth.getInstance()
                        val user = auth.currentUser
                            ?: auth.signInAnonymously().await().user
                            ?: error("Firebase did not return a user")

                        Log.d("FirebaseSetup", "Signed in: ${user.uid}")
                        signedIn = true
                    } catch (error: CancellationException) {
                        throw error
                    } catch (error: Exception) {
                        Log.e("FirebaseSetup", "Sign in failed", error)
                        errorMessage =
                            "Could not connect. Check your internet and retry."
                    }
                }

                if (signedIn) {
                    MainScreen()
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(
                            16.dp,
                            Alignment.CenterVertically
                        )
                    ) {
                        if (errorMessage == null) {
                            CircularProgressIndicator()
                            Text("Connecting...")
                        } else {
                            Text(errorMessage.orEmpty())

                            Button(onClick = { attempt++ }) {
                                Text("Retry")
                            }
                        }
                    }
                }
            }
        }
    }
}