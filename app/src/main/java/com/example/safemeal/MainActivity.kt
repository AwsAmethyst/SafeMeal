package com.example.safemeal


import android.os.Bundle
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
import androidx.compose.ui.Modifier
import com.example.safemeal.ui.theme.SafeMealTheme
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.safemeal.screens.LoginPage
import com.example.safemeal.screens.SignUpPage

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

    Scaffold(modifier = Modifier.fillMaxSize()) { innerpadding ->
        NavHost(
            navController = navController,
            startDestination = "login",
            modifier = Modifier.padding(innerpadding)
        ){
            composable(
                route = "login",
                exitTransition = {
                    slideOutHorizontally(targetOffsetX = { -1000 }, animationSpec = tween(500)) + fadeOut()
                },
                popEnterTransition = {
                    slideInHorizontally(initialOffsetX = { -1000 }, animationSpec = tween(500)) + fadeIn()
                }
            ){
                LoginPage(

                    onNavigateToSignUp = { navController.navigate("signup") }
                )
            }

            composable(
                route = "signup",
                // When we enter SignUp from Login
                enterTransition = {
                    slideInHorizontally(initialOffsetX = { 1000 }, animationSpec = tween(500)) + fadeIn()
                },
                // When we leave SignUp to go back to Log in
                popExitTransition = {
                    slideOutHorizontally(targetOffsetX = { 1000 }, animationSpec = tween(500)) + fadeOut()
                }
            ){
                SignUpPage(
                    onBackToLogin = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}




