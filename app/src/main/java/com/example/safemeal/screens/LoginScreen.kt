package com.example.safemeal.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicSecureTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.TextObfuscationMode
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.safemeal.AppwriteManger
import com.example.safemeal.R
import com.example.safemeal.ui.theme.DarkGreenMain
import com.example.safemeal.ui.theme.GreenMain
import com.example.safemeal.ui.theme.GreyMain
import com.example.safemeal.ui.theme.SafeMealTheme
import com.example.safemeal.ui.theme.WhiteMain
import kotlinx.coroutines.launch

@Composable
fun LoginPage(onNavigateToSignUp: () -> Unit, onLoginSuccess: () -> Unit) {
    var text by remember { mutableStateOf("") }
    val passState = remember { TextFieldState() }
    var showPassword by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        //verticalArrangement = Arrangement.Bottom,
        modifier = Modifier
            .fillMaxSize()
            .background(DarkGreenMain)

    ) {
        Image(
            painter = painterResource(id = R.drawable.logo2),
            contentDescription = null,
            modifier = Modifier
                .width(300.dp)
        )

        Column(

            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                .background(WhiteMain)
                .padding(20.dp)
        )
        {
            Text(
                text = "Welcome Back",
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier
                    .padding(top = 20.dp, start = 20.dp)
            )
            Text(
                fontWeight = FontWeight.Light,
                color = GreyMain,
                text = "Sign in to find the safest Restaurant for you",
                modifier = Modifier.padding(start = 20.dp, bottom = 20.dp)
            )
            Text(
                fontWeight = FontWeight.Bold,
                text = "Email Address",
                modifier = Modifier
                    .padding(start = 25.dp)
            )
            TextField(
                value = text, // Bind the current value
                onValueChange = { newText -> text = newText }, // Update the state when text changes
                label = { Text("Enter your email address") }, // Optional label/hint
                placeholder = { Text("") },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    cursorColor = Color.Black,
                    focusedIndicatorColor = Color.Transparent,    // Hides the line when clicked
                    unfocusedIndicatorColor = Color.Transparent,  // Hides the line when not clicked
                    focusedLabelColor = Color.Gray,
                    unfocusedLabelColor = Color.LightGray
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp)
                    .border(1.dp, GreyMain, shape = RoundedCornerShape(15.dp)),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,      // Shows the '@' key
                    imeAction = ImeAction.Next
                ),

                )
            Text(
                fontWeight = FontWeight.Bold,
                text = "Password",
                modifier = Modifier
                    .padding(start = 25.dp, top = 25.dp)
            )
            BasicSecureTextField(
                state = passState,
                textObfuscationMode = if(showPassword){
                    TextObfuscationMode.Visible
                }else{
                    TextObfuscationMode.RevealLastTyped
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp)
                    .border(1.dp, GreyMain, shape = RoundedCornerShape(15.dp)),
                decorator = {innerTextField ->
                    Box(modifier = Modifier.fillMaxWidth()){
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .padding(start = 16.dp, end = 48.dp)

                        ){
                            if (passState.text.isEmpty()) {
                                Text(
                                    fontWeight = FontWeight.ExtraLight,
                                    text = "Enter your Password",
                                    color = Color.Gray
                                )}
                            innerTextField()
                        }
                        Icon(
                            if(showPassword){
                                painterResource(R.drawable.visibility)
                            }else{
                                painterResource(R.drawable.visibility_off)
                            },
                            contentDescription = "Toggle Visibility",
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .requiredSize(48.dp).padding(16.dp)
                                .clickable { showPassword = !showPassword }
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next)
                )
            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = Color.Red,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(start = 25.dp, top = 8.dp)
                )
            }
            TextButton(
                onClick = {}
            ) {
                Text(
                    text = "Forgot Password?",
                    color = GreenMain,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(top = 1.dp, start = 20.dp)
                )
            }



            Button(
                onClick = {
                    val sanitizedEmail = text.trim().lowercase()
                    val password = passState.text.toString() // Convert TextFieldState to String

                    if (sanitizedEmail.isEmpty() || password.isEmpty()) {
                        errorMessage = "Please fill in all fields"
                        return@Button
                    }

                    scope.launch {
                        isLoading = true
                        errorMessage = null
                        try {
                            // 1. Try to delete any "Ghost" sessions first to clear the 'Guest' role
                            try {
                                AppwriteManger.AppwriteManager.account.deleteSession("current")
                            } catch (e: Exception) {
                                // Ignore if no session exists
                            }

                            // 2. Perform the actual Login
                            AppwriteManger.AppwriteManager.account.createEmailPasswordSession(
                                email = sanitizedEmail,
                                password = password
                            )

                            isLoading = false
                            onLoginSuccess()
                        } catch (e: Exception) {
                            isLoading = false
                            Log.e("AppwriteAuth", "Login Error: ${e.message}", e)

                            // 3. Expanded Error Messaging for Debugging
                            errorMessage = when {
                                e.message?.contains("401") == true -> "Incorrect email or password."
                                e.message?.contains("403") == true -> "Platform access denied. Check Appwrite Console Package Name."
                                e.message?.contains("404") == true -> "Project ID or Endpoint is incorrect."
                                e.message?.contains("network", true) == true || e.message?.contains("Handshake", true) == true ->
                                    "Network error. Check if your phone has Internet."
                                else -> e.message ?: "An unexpected error occurred."
                            }
                        }
                    }
                },
                enabled = !isLoading && text.isNotEmpty() && passState.text.isNotEmpty(),
                shape = RoundedCornerShape(15.dp),
                colors = ButtonColors(
                    containerColor = GreenMain,
                    contentColor = WhiteMain,
                    disabledContainerColor = GreyMain,
                    disabledContentColor = GreenMain,
                ),
                modifier = Modifier
                    .padding(start = 20.dp, end = 20.dp)
                    .height(50.dp)
                    .fillMaxWidth()
                //.background(GreyMain)

            ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = WhiteMain, modifier = Modifier.size(24.dp))
                    } else {
                        Text(text = "Sign In", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = WhiteMain)
                    }
            }

            val annotatedText = buildAnnotatedString {
                append("Don't have an account ")

                pushStringAnnotation(tag = "signup", annotation = "navigate_to_signup")
                withStyle(
                    style = SpanStyle(
                        color = GreenMain,
                        fontWeight = FontWeight.Bold,
                    )
                ) {
                    append("Sign Up")
                }
                pop() // Important: close the annotation
            }
            Text(
                buildAnnotatedString {
                    append("Don't have an account ")
                    withLink(
                        LinkAnnotation.Clickable(
                            tag = "signup",
                            linkInteractionListener = {
                                    onNavigateToSignUp()
                            }
                        )) {
                        withStyle(
                            style = SpanStyle(
                                color = GreenMain,
                                fontWeight = FontWeight.Bold
                            )
                        ) {
                            append("Sign Up")
                        }
                    }
                },
                modifier = Modifier
                    .padding(start = 20.dp)
            )

        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SafeMealTheme {
        LoginPage(onNavigateToSignUp = {}, onLoginSuccess = {})
    }
}