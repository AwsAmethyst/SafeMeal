package com.example.safemeal.screens

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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
fun SignUpPage(onBackToLogin: () -> Unit, onSignUpSuccess: () -> Unit) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    val passState = remember { TextFieldState() }
    var showPassword by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

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
                .clip(RoundedCornerShape(20.dp))
                .background(WhiteMain)
                .padding(20.dp)
        )
        {
            Text(
                text = "Join SafeMeal",
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier
                    .padding(top = 20.dp, start = 20.dp)
            )
            Text(
                fontWeight = FontWeight.Light,
                color = GreyMain,
                text = "Sign up to find the safest Restaurant for you",
                modifier = Modifier.padding(start = 20.dp, bottom = 20.dp)
            )
            Text(
                fontWeight = FontWeight.Bold,
                text = "Full Name",
                modifier = Modifier
                    .padding(start = 25.dp)
            )
            TextField(
                value = username, // Bind the current value
                onValueChange = { newText -> username = newText }, // Update the state when text changes
                label = { Text("Enter your Full Name") }, // Optional label/hint
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
                )
            Text(
                fontWeight = FontWeight.Bold,
                text = "Email Address",
                modifier = Modifier
                    .padding(start = 25.dp, top = 25.dp)
            )
            TextField(
                value = email, // Bind the current value
                onValueChange = { newText -> email = newText }, // Update the state when text changes
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
                    keyboardType = KeyboardType.Text,      // Shows the '@' key
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

            Button(
                onClick = {
                    scope.launch {
                        isLoading = true
                        try {

                            AppwriteManger.AppwriteManager.account.create(
                                userId = io.appwrite.ID.unique(),
                                email = email,
                                password = passState.text.toString(),
                                name = username
                            )


                            AppwriteManger.AppwriteManager.account.createEmailPasswordSession(
                                email = email,
                                password = passState.text.toString()
                            )

                            onSignUpSuccess() // Navigate to home
                        } catch (e: Exception) {
                            // Handle errors like 'email already exists'
                            println("Signup Error: ${e.message}")
                        } finally {
                            isLoading = false
                        }
                    }
                },
                enabled = !isLoading && email.isNotEmpty() && passState.text.isNotEmpty(),
                shape = RoundedCornerShape(15.dp),
                colors = ButtonColors(
                    containerColor = GreenMain,
                    contentColor = WhiteMain,
                    disabledContainerColor = GreyMain,
                    disabledContentColor = GreenMain,
                ),
                modifier = Modifier
                    .padding(start = 20.dp, end = 20.dp, top = 25.dp)
                    .height(50.dp)
                    .fillMaxWidth()
                //.background(GreyMain)

            ) {
                if (isLoading) {
                    androidx.compose.material3.CircularProgressIndicator(
                        color = WhiteMain,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(text = "Sign Up", fontSize = 20.sp)
                }
                /*Row(
                    verticalAlignment = Alignment.CenterVertically,
                    //color = GreenMain
                ) {
                    Text(
                        text = "Sign Up",
                        fontSize = 20.sp,
                        modifier = Modifier
                    )
                }*/

            }
            Text(
                buildAnnotatedString {
                    append("Have an account ")
                    withLink(
                        LinkAnnotation.Clickable(
                            tag = "signup",
                            linkInteractionListener = {
                                onBackToLogin()
                            }
                        )) {
                        withStyle(
                            style = SpanStyle(
                                color = GreenMain,
                                fontWeight = FontWeight.Bold
                            )
                        ) {
                            append("Sign In")
                        }
                    }
                },
                modifier = Modifier
                    .padding(start = 20.dp, top = 20.dp)
            )

        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview2() {
    SafeMealTheme {
        SignUpPage(
            onBackToLogin = { /* Do nothing in preview */ },
            onSignUpSuccess = { /* Do nothing in preview */ }
        )
    }
}