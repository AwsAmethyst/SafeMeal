package com.example.safemeal.screens
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.safemeal.AppwriteManger
import com.example.safemeal.R
import com.example.safemeal.ui.theme.GreenMain
import com.example.safemeal.ui.theme.GreyMain
import com.example.safemeal.ui.theme.Manrope
import com.example.safemeal.ui.theme.SafeMealTheme
import com.example.safemeal.ui.theme.WhiteMain
import kotlinx.coroutines.launch

@Composable
fun ProfilePage(innerPadding: PaddingValues, onLogout: () -> Unit) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var userName by remember { mutableStateOf("Loading...") }
    //var userEmail by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        try {
            // FR13: Fetch current user session details
            val user = AppwriteManger.AppwriteManager.account.get()
            userName = user.name
            //userEmail = user.email
        } catch (e: Exception) {
            userName = "Guest User"
        }
    }
    Column{
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .background(GreenMain)
        ) {
            Spacer(
                modifier = Modifier
                    .height(50.dp)
            )
            Box(contentAlignment = Alignment.BottomEnd) {
                Image(
                    painter = painterResource(R.drawable.pfp),
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .border(4.dp, GreyMain.copy(alpha = 0.8f), CircleShape),
                    contentScale = ContentScale.Crop
                )
                FilledIconButton(
                    onClick = { /* Edit profile */ },
                    modifier = Modifier.size(36.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = GreyMain)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.edit),
                        contentDescription = "Edit",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Text(
                text = userName,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier
                    .align(alignment = Alignment.CenterHorizontally)
                    .padding(10.dp),
                color = WhiteMain
            )
            Spacer(
                modifier = Modifier
                    .height(15.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .background(WhiteMain)

            ) {
                ProfileFilterSection()
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                            alpha = 0.3f
                        )
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column {
                        ProfileMenuItem("Saved Restaurants", R.drawable.bookmark)
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            thickness = 0.5.dp
                        )
                        ProfileMenuItem("Suggest Restaurant", R.drawable.add)
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            thickness = 0.5.dp
                        )
                        ProfileMenuItem("Review Activity", R.drawable.reviews)
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                OutlinedButton(
                    onClick = { showLogoutDialog = true;  },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Gray)
                ) {
                    Text(
                        "LOG OUT",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text(
                    text = "Log Out?",
                    fontFamily = Manrope,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to log out of SafeMeal?",
                    fontFamily = Manrope,
                    fontWeight = FontWeight.Light
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        scope.launch {
                            try {
                                // FR13: Terminate the current Appwrite session
                                AppwriteManger.AppwriteManager.account.deleteSession("current")

                                // Navigate back to log in and clear navigation history
                                onLogout()
                            } catch (e: Exception) {
                                // Handle potential network errors (NFR1)
                                Log.e("Logout", "Error logging out: ${e.message}")
                            }
                        }
                        // Add your Appwrite logout logic here
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Log Out")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            },
            containerColor = WhiteMain,
            shape = RoundedCornerShape(16.dp)
        )
    }
    }


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileFilterSection() {
    val dietaryOptions = listOf("Sattvic", "Halal", "Kosher")
    var selectedOption by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(Unit) {
        try {
            val prefs = AppwriteManger.AppwriteManager.account.getPrefs()
            selectedOption = prefs.data["dietary_choice"] as? String
        } catch (e: Exception) { /* Handle error */ }
    }
    Column(
        modifier = Modifier
        .padding(16.dp)
        ) {
        Text(
            text = "DIETARY PREFERENCE",
            //style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp, start = 5.dp)
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            dietaryOptions.forEach { option ->
                // 2. Check if this specific option is the selected one
                val isSelected = selectedOption == option

                FilterChip(
                    selected = isSelected,
                    onClick = {
                        // 3. Logic: If already selected, deselect it; otherwise, select it
                        selectedOption = if (isSelected) null else option
                    },
                    label = {
                        Text(
                            text = option,
                            fontWeight = FontWeight.Light,
                            fontFamily = Manrope
                        )
                    },
                    leadingIcon = if (isSelected) {
                        { Icon(painter = painterResource(R.drawable.check), contentDescription = null, modifier = Modifier.size(18.dp)) }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GreenMain.copy(alpha = 0.2f),
                        selectedLabelColor = GreenMain
                    )
                )
                }
            }
            Button(

                onClick = {
                    scope.launch {
                        try {
                            // Save preference to Appwrite Account
                            val currentPrefs = mapOf("dietary_choice" to selectedOption)
                            AppwriteManger.AppwriteManager.account.updatePrefs(prefs = currentPrefs)

                            android.widget.Toast.makeText(
                                context,
                                "Preference updated to $selectedOption",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        } catch (e: Exception) {
                            Log.e("Prefs", "Failed to save: ${e.message}")
                        }
                    }
                },
                modifier = Modifier
                    .padding(top = 15.dp)
                    .align(Alignment.CenterHorizontally)
                ,
                colors = ButtonColors(
                    containerColor = GreenMain,
                    contentColor = WhiteMain,
                    disabledContainerColor = GreyMain,
                    disabledContentColor = GreenMain,
                ),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    //color = GreenMain
                ) {
                    Text(
                        text = "Save",
                        fontSize = 15.sp,
                        modifier = Modifier
                    )
                }
            }
        }
    }

@Composable
fun ProfileMenuItem(text: String, @DrawableRes iconResource: Int) {
    ListItem(
        headlineContent = { Text(text, fontWeight = FontWeight.Bold) },
        leadingContent = {
            Surface(
                color = Color(0xFF17CF63).copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(painterResource(iconResource), contentDescription = null, tint = GreenMain, modifier = Modifier.size(20.dp))
                }
            }
        },
        trailingContent = { Icon(painter = painterResource(R.drawable.chevron_right), contentDescription = null, tint = Color.LightGray) },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}
@Preview
@Composable
fun ProfilePreview(){
    SafeMealTheme {
        ProfilePage(innerPadding = PaddingValues(0.dp),onLogout = {})
    }
}