package com.example.safemeal.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.example.safemeal.R
import com.example.safemeal.ui.theme.DarkGreenMain
import com.example.safemeal.ui.theme.GreenMain
import com.example.safemeal.ui.theme.GreyMain
import com.example.safemeal.ui.theme.SafeMealTheme
import com.example.safemeal.ui.theme.WhiteMain

@Composable
fun SignUpPage(onBackToLogin: () -> Unit) {
    var text by remember { mutableStateOf("") }
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
                text = "Email Address",
                modifier = Modifier
                    .padding(start = 25.dp, top = 25.dp)
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
            TextField(
                value = text, // Bind the current value
                onValueChange = { newText -> text = newText }, // Update the state when text changes
                label = { Text("Enter your password") }, // Optional label/hint
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
                    keyboardType = KeyboardType.Password,      // Shows the '@' key
                    imeAction = ImeAction.Done
                ),
            )

            Button(
                onClick = {},
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    //color = GreenMain
                ) {
                    Text(
                        text = "Sign Up",
                        fontSize = 20.sp,
                        modifier = Modifier
                    )
                }

            }

            /*            Text(

                            text = "Or Sign Up with",
                            textAlign = TextAlign.Center,
                            color = GreyMain,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(15.dp)
                        )
            */
//Add google sign in here
// Define your annotated string first
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
        SignUpPage(onBackToLogin = {})
    }
}