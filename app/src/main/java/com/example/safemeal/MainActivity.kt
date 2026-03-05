package com.example.safemeal


import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.safemeal.ui.theme.SafeMealTheme
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.safemeal.screens.HomePage
import com.example.safemeal.screens.LoginPage
import com.example.safemeal.screens.SignUpPage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppwriteManger.AppwriteManager.init(applicationContext)
        enableEdgeToEdge()
        setContent {
            SafeMealTheme {
                SafeMealApp()
            }
        }
    }
}
@Composable
fun SafeMealApp(){
    val navController = rememberNavController()
    var startDestination by remember { mutableStateOf("loading") }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        try {
            // Check if a session exists
            AppwriteManger.AppwriteManager.account.get()
            startDestination = "home"
        } catch (e: Exception) {
            // If no session or error, go to login
            startDestination = "login"
        }
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerpadding ->
        if(startDestination != "loading") {
            NavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier.padding(innerpadding)
            ) {
                composable(
                    route = "login",
                    exitTransition = {
                        slideOutHorizontally(
                            targetOffsetX = { -1000 },
                            animationSpec = tween(500)
                        ) + fadeOut()
                    },
                    popEnterTransition = {
                        slideInHorizontally(
                            initialOffsetX = { -1000 },
                            animationSpec = tween(500)
                        ) + fadeIn()
                    }
                ) {
                    LoginPage(
                        onNavigateToSignUp = {
                            navController.navigate("signup")
                        },
                        onLoginSuccess = {
                            navController.navigate("home") {
                                popUpTo("login") { inclusive = true }
                            }
                        }
                    )
                }

                composable(
                    route = "signup",
                    // When we enter SignUp from Login
                    enterTransition = {
                        slideInHorizontally(
                            initialOffsetX = { 1000 },
                            animationSpec = tween(500)
                        ) + fadeIn()
                    },
                    // When we leave SignUp to go back to Log in
                    popExitTransition = {
                        slideOutHorizontally(
                            targetOffsetX = { 1000 },
                            animationSpec = tween(500)
                        ) + fadeOut()
                    }
                ) {
                    SignUpPage(
                        onBackToLogin = {
                            navController.popBackStack() // Returns to the Login screen
                        },
                        onSignUpSuccess = {
                            // FR13: After account creation, send them to the Home screen
                            navController.navigate("home") {
                                // Clear the signup and login from history so they can't go "back" to them
                                popUpTo("login") { inclusive = true }
                            }
                        }
                    )
                }
                composable(
                    route = "home"
                ) {
                    HomePage(mainNavController = navController)
                }

                composable(
                    route = "logout"
                ) { }

                composable(route = "loading") {

                }
            }
        }
    }
}




