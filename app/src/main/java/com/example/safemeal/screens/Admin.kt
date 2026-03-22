package com.example.safemeal.screens


import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.safemeal.R
import com.example.safemeal.data.restaurant.Restaurant
import com.example.safemeal.data.restaurant.RestaurantViewModel
import com.example.safemeal.ui.theme.GreenMain
import com.example.safemeal.ui.theme.GreyMain
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.example.safemeal.data.menu.MenuItem
import com.example.safemeal.ui.theme.Manrope


@Composable
fun ManageRestaurantScreen(
    viewModel: RestaurantViewModel, // Pass this in
    existingRestaurant: Restaurant? = null,
    onSave: (Restaurant, Boolean) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(existingRestaurant?.name ?: "") }
    var address by remember { mutableStateOf(existingRestaurant?.address ?: "") }
    var tags by remember { mutableStateOf(existingRestaurant?.tags ?: "") }
    var hours by remember { mutableStateOf(existingRestaurant?.hours ?: "") }
    var menuJson by remember { mutableStateOf(existingRestaurant?.menuJson ?: "[]") }
    var latitude by remember { mutableStateOf(existingRestaurant?.latitude?.toString() ?: "0.0") }
    var longitude by remember { mutableStateOf(existingRestaurant?.longitude?.toString() ?: "0.0") }
    var selectedTag by remember { mutableStateOf(existingRestaurant?.tags ?: "")}
    // Initialize the ViewModel's image ID if we are editing
    LaunchedEffect(existingRestaurant) {
        if (existingRestaurant != null) {
            viewModel.imageUploadId = existingRestaurant.imgid
        } else {
            // Clear the ID if we are adding a fresh restaurant
            viewModel.imageUploadId = ""
        }
    }

    // 2. The Image Picker Launcher
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.uploadRestaurantImage(context, it) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    painterResource(R.drawable.arrow_back),
                    contentDescription = "Back",
                    tint = Color.Black
                )
            }
            Text(
                text = if (existingRestaurant == null) "Add New Restaurant" else "Edit Restaurant",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = if (existingRestaurant == null) "Add New Restaurant" else "Update Restaurant",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Text Fields
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Restaurant Name") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Address") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(value = hours, onValueChange = { hours = it }, label = { Text("Hours (e.g. 8AM - 10PM)") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(16.dp))
        RestaurantTagDropdown(
            selectedTag = selectedTag,
            onTagSelected = { selectedTag = it }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = latitude,
                onValueChange = { latitude = it },
                label = { Text("Latitude") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            OutlinedTextField(
                value = longitude,
                onValueChange = { longitude = it },
                label = { Text("Longitude") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        MenuEditor(
            initialJson = menuJson,
            onMenuChanged = { newJson ->
                menuJson = newJson // This updates your state variable used for saving
            }
        )
        Spacer(modifier = Modifier.height(24.dp))

        // 3. Image Upload Area (Updated with logic)
        Text("Restaurant Cover Image", fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.LightGray.copy(alpha = 0.3f))
                .clickable { launcher.launch("image/*") }, // FIXED: Calls launcher
            contentAlignment = Alignment.Center
        ) {
            if (viewModel.isUploading) {
                CircularProgressIndicator(color = GreenMain)
            } else if (viewModel.imageUploadId.isNotEmpty()) {
                Text("Image Uploaded: ${viewModel.imageUploadId}", color = GreenMain, fontWeight = FontWeight.Bold)
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(painterResource(R.drawable.location), contentDescription = null, tint = GreyMain)
                    Text("Tap to Upload Photo", color = GreyMain)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
// Add this right before the final "Save Restaurant" Button
        if (existingRestaurant != null) {
            androidx.compose.material3.TextButton(
                onClick = {
                   onBack()
                },
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("Delete Restaurant", color = Color.Red, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = {
                // Create the object from your current text field states
                val restaurantToSave = Restaurant(
                    id = existingRestaurant?.id ?: "", // Use old ID if editing, empty if new
                    name = name,
                    address = address,
                    tags = selectedTag,
                    latitude = latitude.toDoubleOrNull() ?: 0.0,
                    longitude = longitude.toDoubleOrNull() ?: 0.0,
                    hours = hours,
                    menuJson = menuJson,
                    imgid = viewModel.imageUploadId
                )

                // Call the ViewModel function
                viewModel.saveRestaurant(
                    restaurant = restaurantToSave,
                    isEdit = existingRestaurant != null
                )

                // Go back to the dashboard/list
                onBack()
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GreenMain)
        ) {
            Text(
                text = if (existingRestaurant == null) "Create Restaurant" else "Update Restaurant",
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        Spacer(Modifier.height(100.dp))
    }
}

@Composable
fun MenuEditor(
    initialJson: String,
    onMenuChanged: (String) -> Unit
) {
    // 1. Parse the initial JSON into a List of MenuItems
    val gson = Gson()
    val listType = object : TypeToken<List<MenuItem>>() {}.type
    val menuItems = remember {
        mutableStateListOf<MenuItem>().apply {
            try {
                addAll(gson.fromJson(initialJson, listType) ?: emptyList())
            } catch (e: Exception) { add(MenuItem()) } // Default if empty
        }
    }

    // 2. Helper to update parent JSON whenever a field changes
    fun updateParent() {
        onMenuChanged(gson.toJson(menuItems))
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Menu Items", fontWeight = FontWeight.Bold, fontSize = 18.sp)

        menuItems.forEachIndexed { index, item ->
            Card(
                modifier = Modifier.padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color.LightGray)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    OutlinedTextField(
                        value = item.name,
                        onValueChange = { menuItems[index] = item.copy(name = it); updateParent() },
                        label = { Text("Dish Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = item.desc,
                        onValueChange = { menuItems[index] = item.copy(desc = it); updateParent() },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = item.price,
                            onValueChange = { menuItems[index] = item.copy(price = it); updateParent() },
                            label = { Text("Price (KSH)") },
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { menuItems.removeAt(index); updateParent() }) {
                            Icon(painterResource(R.drawable.close), contentDescription = "Remove", tint = Color.Red)
                        }
                    }
                }
            }
        }

        Button(
            onClick = { menuItems.add(MenuItem()); updateParent() },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("+ Add Another Dish")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantTagDropdown(
    selectedTag: String,
    onTagSelected: (String) -> Unit
) {
    val tagOptions = listOf("Sattvic", "Halal", "Kosher")
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(
            text = "Safety Category",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = GreyMain,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedTag,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                    focusedBorderColor = GreenMain,
                    unfocusedBorderColor = Color.LightGray
                ),
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                tagOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option, fontFamily = Manrope) },
                        onClick = {
                            onTagSelected(option)
                            expanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }
    }
}