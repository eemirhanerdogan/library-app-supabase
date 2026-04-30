package com.turkcell.libraryapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.turkcell.libraryapp.ui.screen.HomeScreen
import com.turkcell.libraryapp.ui.screen.LoginScreen
import com.turkcell.libraryapp.ui.screen.RegisterScreen
import com.turkcell.libraryapp.ui.screen.SplashScreen
import com.turkcell.libraryapp.ui.viewmodel.AuthViewModel
import com.turkcell.libraryapp.ui.viewmodel.BookViewModel
import com.turkcell.libraryapp.ui.viewmodel.SessionState

@Composable
fun NavGraph(navController: NavHostController = rememberNavController()) {
    val authViewModel: AuthViewModel = viewModel()
    val bookViewModel: BookViewModel = viewModel()
    val sessionState by authViewModel.sessionState.collectAsState()

    NavHost(navController = navController, startDestination = Screen.Splash.route)
    {
        composable(Screen.Splash.route) {
            SplashScreen(authViewModel,
                onAuthenticated = { role ->
                    navController.navigate(Screen.Homepage.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onUnauthenticated = {
                    navController.navigate(Screen.Login.route)
                    {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                })
        }
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                onLoginSuccess = { role ->
                    navController.navigate(Screen.Homepage.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                authViewModel
            )
        }
        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateToLogin = { navController.navigate(Screen.Login.route) },
                authViewModel
            )
        }
        composable(Screen.Homepage.route) {
            // Çıkış yapıldığında otomatik olarak Login ekranına dönmek için dinleyici
            LaunchedEffect(sessionState) {
                if (sessionState is SessionState.Unauthenticated) {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Homepage.route) { inclusive = true }
                    }
                }
            }

            HomeScreen(authViewModel, bookViewModel)
        }
    }
}
