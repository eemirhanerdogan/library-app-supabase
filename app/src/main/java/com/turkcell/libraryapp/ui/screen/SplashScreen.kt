package com.turkcell.libraryapp.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.turkcell.libraryapp.ui.viewmodel.AuthViewModel
import com.turkcell.libraryapp.ui.viewmodel.SessionState

@Composable
fun SplashScreen(
    authViewModel: AuthViewModel,
    onAuthenticated: (role: String) -> Unit,
    onUnauthenticated: () -> Unit
) {
    val sessionState by authViewModel.sessionState.collectAsState()

    LaunchedEffect(sessionState) {
        when (sessionState) {
            is SessionState.Authenticated -> {
                onAuthenticated((sessionState as SessionState.Authenticated).role)
            }
            is SessionState.Unauthenticated -> {
                onUnauthenticated()
            }
            else -> {}
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}
