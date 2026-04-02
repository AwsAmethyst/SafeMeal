package com.example.safemeal.screens

import android.graphics.Typeface
import android.util.Log
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.safemeal.R
import com.example.safemeal.data.restaurant.RestaurantViewModel
import com.example.safemeal.ui.theme.GreyMain
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.nativeCanvas
import com.example.safemeal.data.admin.AdminRepository
import com.example.safemeal.ui.theme.Manrope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardPage(
    navController: NavController,
    viewModel: RestaurantViewModel
) {
    // 1. DATA STATES
    var stats by remember { mutableStateOf<AdminRepository.AdminStats?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // 2. FETCH REAL DATA VIA REPOSITORY
    LaunchedEffect(Unit) {
        isLoading = true
        stats = AdminRepository.fetchDashboardStats()
        isLoading = false
    }

    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("ADMIN CONTROL PANEL",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(painterResource(R.drawable.arrow_back), contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = BackgroundLight)
            )
        }
    ) { innerPadding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryGreen)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
            ) {
                // --- ACTION SECTION: ADD RESTAURANT ---
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .clickable {
                            viewModel.selectedRestaurantForDetails = null
                            navController.navigate("manage_restaurant")
                        },
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFBF4F00))
                ) {
                    Row(
                        modifier = Modifier.padding(24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.restaurant),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text("RESTAURANT MANAGEMENT", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("Add New Restaurant", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }

                // --- GLOBAL STATS ROW (REAL DATA) ---
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    AdminStatBox("TOTAL STORES", (stats?.totalRestaurants ?: 0).toString(), Modifier.weight(1f))
                    // Mocking Active Users for now as Appwrite Server SDK is needed for real User counts
                    AdminStatBox("ACTIVE USERS", (stats?.totalUsers ?: 1).toString(), Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(24.dp))

                // --- GRAPH 1: RESTAURANT ONBOARDING (REAL TREND) ---
                Text("Onboarding Trend", modifier = Modifier.padding(start = 24.dp, bottom = 8.dp), fontWeight = FontWeight.Bold, color = GreyMain)
                UserImpactLineGraph(reviewData = stats?.restaurantGrowth ?: emptyList())

                // --- GRAPH 2: USER EXPANSION (REAL TOTAL) ---
                Text("Community Expansion", modifier = Modifier.padding(start = 24.dp, top = 16.dp, bottom = 8.dp), fontWeight = FontWeight.Bold, color = GreyMain)
                // Using the growth logic to show expansion trend
                TotalUsersGrowthGraph(userData = stats?.userGrowth ?: listOf("Feb" to 1, "Mar" to 1, "Apr" to 1))

                // --- GRAPH 3: DIETARY DISTRIBUTION (REAL DATA) ---
                if (stats != null) {
                    DietaryDistributionChart(data = stats!!.dietaryData)
                }

                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
fun DietaryDistributionChart(data: List<Pair<String, Float>>) {
    val colors = listOf(
        Color(0xFF2E7D32), // Deep Green (Halal)
        Color(0xFFE65100), // Deep Orange (Sattvic)
        Color(0xFF1976D2)  // Bright Blue (Kosher)
    )

    Card(
        modifier = Modifier.fillMaxWidth().padding(24.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text("Certification Breakdown", fontWeight = FontWeight.Bold, color = GreyMain)
            Spacer(Modifier.height(20.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(120.dp), contentAlignment = Alignment.Center) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        var startAngle = 0f
                        data.forEachIndexed { index, pair ->
                            val sweepAngle = (pair.second / 100f) * 360f
                            drawArc(
                                color = colors.getOrElse(index) { Color.Gray },
                                startAngle = startAngle,
                                sweepAngle = sweepAngle,
                                useCenter = true
                            )
                            startAngle += sweepAngle
                        }
                    }
                }

                Spacer(Modifier.width(24.dp))

                // Legend
                Column {
                    data.forEachIndexed { index, pair ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(10.dp).background(colors.getOrElse(index) { Color.Gray }, RoundedCornerShape(2.dp)))
                            Spacer(Modifier.width(8.dp))
                            Text("${pair.first}: ${pair.second.toInt()}%", fontSize = 12.sp, color = GreyMain)
                        }
                        Spacer(Modifier.height(4.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun AdminStatBox(label: String, value: String, modifier: Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = PrimaryGreen)
        }
    }
}

@Composable
fun TotalUsersGrowthGraph(userData: List<Pair<String, Int>>) {
    val maxUsers = userData.maxOfOrNull { it.second }?.coerceAtLeast(1) ?: 1
    val graphMax = maxUsers * 1.4f
    val userBlue = Color(0xFF2196F3)

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("COMMUNITY EXPANSION (TOTAL USERS)", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.ExtraBold, color = userBlue, fontFamily = Manrope)
            Spacer(modifier = Modifier.height(30.dp))
            Canvas(modifier = Modifier.fillMaxWidth().height(150.dp).padding(start = 40.dp, bottom = 25.dp, end = 20.dp)) {
                val width = size.width
                val height = size.height
                val xSpacing = width / (userData.size - 1)
                val points = userData.mapIndexed { index, pair ->
                    Offset(index * xSpacing, height - ((pair.second.toFloat() / graphMax) * height))
                }
                val fillPath = Path().apply {
                    moveTo(0f, height)
                    points.forEach { lineTo(it.x, it.y) }
                    lineTo(width, height)
                    close()
                }
                drawPath(path = fillPath, brush = androidx.compose.ui.graphics.Brush.verticalGradient(colors = listOf(userBlue.copy(alpha = 0.3f), Color.Transparent), startY = points.minOf { it.y }, endY = height))
                val linePath = Path().apply {
                    moveTo(points[0].x, points[0].y)
                    for (i in 1 until points.size) { lineTo(points[i].x, points[i].y) }
                }
                drawPath(path = linePath, color = userBlue, style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round))
                drawContext.canvas.nativeCanvas.drawText(maxUsers.toString(), -20f, points.minOf { it.y } + 10f, android.graphics.Paint().apply { color = userBlue.toArgb(); textSize = 12.sp.toPx(); textAlign = android.graphics.Paint.Align.RIGHT; typeface = android.graphics.Typeface.DEFAULT_BOLD })
                userData.forEachIndexed { index, pair ->
                    drawContext.canvas.nativeCanvas.drawText(pair.first.uppercase(), index * xSpacing, height + 40f, android.graphics.Paint().apply { color = Color.Gray.toArgb(); textSize = 10.sp.toPx(); textAlign = android.graphics.Paint.Align.CENTER })
                }
            }
        }
    }
}

