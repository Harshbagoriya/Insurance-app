package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.OceanPrimary
import com.example.ui.theme.OceanSecondary
import com.example.ui.theme.OceanTertiary
import com.example.ui.theme.WarningGold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedType by remember { mutableStateOf("HEALTH") } // HEALTH, VEHICLE, LIFE, HOME
    var age by remember { mutableStateOf(30f) }
    var tobaccoUser by remember { mutableStateOf(false) }
    var preExistingConditions by remember { mutableStateOf(false) }
    var coverageLevel by remember { mutableStateOf("Standard") } // Value, Standard, Premium
    var coverageAmount by remember { mutableStateOf(250000f) }

    // Vehicle Specifics
    var vehicleCategory by remember { mutableStateOf("Sedan") } // Sedan, SUV, Sports, Bike
    var drivingRecord by remember { mutableStateOf("Clean") } // Clean, Minor, Major

    // Calculate premium based on internal formulas
    val computedPremium = remember(selectedType, age, tobaccoUser, preExistingConditions, coverageLevel, coverageAmount, vehicleCategory, drivingRecord) {
        var basePremium = when (selectedType) {
            "HEALTH" -> 50.0
            "VEHICLE" -> 35.0
            "LIFE" -> 25.0
            else -> 15.0 // HOME
        }

        // Adjust for coverage amount scale
        val coverageScale = coverageAmount / 150000.0
        basePremium += (coverageScale * 12.0)

        // Age effect factor
        basePremium *= when {
            age < 25 -> 1.35
            age in 25f..49f -> 1.0
            age in 50f..65f -> 1.25
            else -> 1.6
        }

        // Particular triggers
        if (selectedType == "HEALTH") {
            if (tobaccoUser) basePremium *= 1.45
            if (preExistingConditions) basePremium *= 1.35
            basePremium *= when (coverageLevel) {
                "Value" -> 0.8
                "Premium" -> 1.35
                else -> 1.0
            }
        } else if (selectedType == "VEHICLE") {
            basePremium *= when (vehicleCategory) {
                "SUV" -> 1.15
                "Sports" -> 1.65
                "Bike" -> 0.65
                else -> 1.0 // Sedan
            }
            basePremium *= when (drivingRecord) {
                "Minor" -> 1.25
                "Major" -> 1.8
                else -> 1.0 // Clean
            }
        } else if (selectedType == "LIFE") {
            if (tobaccoUser) basePremium *= 1.85
            if (preExistingConditions) basePremium *= 1.55
            if (age > 50) basePremium *= 1.9
        }

        basePremium
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Premium Calculator", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("calculator_back_btn")) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Description Header
            Text(
                text = "Simulate plan pricing instantly. Customize coverage filters and see real-time estimates adjusted to your precise risk parameters.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 4.dp)
            )

            // category selectors row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CategoryTabItem(
                    title = "Health",
                    icon = Icons.Default.MedicalServices,
                    isSelected = selectedType == "HEALTH",
                    onClick = { selectedType = "HEALTH" },
                    modifier = Modifier.weight(1f).testTag("tab_health")
                )
                CategoryTabItem(
                    title = "Vehicle",
                    icon = Icons.Default.DirectionsCar,
                    isSelected = selectedType == "VEHICLE",
                    onClick = { selectedType = "VEHICLE" },
                    modifier = Modifier.weight(1f).testTag("tab_vehicle")
                )
                CategoryTabItem(
                    title = "Life",
                    icon = Icons.Default.Favorite,
                    isSelected = selectedType == "LIFE",
                    onClick = { selectedType = "LIFE" },
                    modifier = Modifier.weight(1f).testTag("tab_life")
                )
                CategoryTabItem(
                    title = "Home",
                    icon = Icons.Default.Home,
                    isSelected = selectedType == "HOME",
                    onClick = { selectedType = "HOME" },
                    modifier = Modifier.weight(1f).testTag("tab_home")
                )
            }

            // Estimations Outcome Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "ESTIMATED PLAN PREMIUM",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "₹${String.format("%.2f", computedPremium)}",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "/month",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            // Input fields card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(18.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // 1. Age Slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Applicant Age",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${age.toInt()} Years old",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 15.sp,
                                modifier = Modifier.testTag("age_readout")
                            )
                        }
                        Slider(
                            value = age,
                            onValueChange = { age = it },
                            valueRange = 18f..85f,
                            steps = 67,
                            modifier = Modifier.testTag("age_slider")
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("18", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                            Text("85", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                        }
                    }

                    // 2. Coverage Limit Slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Requested Coverage Limit",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "₹${String.format("%,d", coverageAmount.toInt())}",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 15.sp,
                                modifier = Modifier.testTag("coverage_readout")
                            )
                        }
                        Slider(
                            value = coverageAmount,
                            onValueChange = { coverageAmount = it },
                            valueRange = 10000f..1000000f,
                            modifier = Modifier.testTag("coverage_slider")
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("₹10,000", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                            Text("₹1,000,000", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                        }
                    }

                    // Category Specific inputs
                    if (selectedType == "HEALTH" || selectedType == "LIFE") {
                        Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                        Text(
                            text = "Health & Habits Metrics",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // Tobacco Switch
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Tobacco / Nicotine Consumption", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                Text("Do you smoke or consume tobacco products regularly?", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                            }
                            Switch(
                                checked = tobaccoUser,
                                onCheckedChange = { tobaccoUser = it },
                                modifier = Modifier.testTag("tobacco_switch")
                            )
                        }

                        // Pre-existing switch
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Pre-existing Medical Conditions", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                Text("Diagnosed conditions like cardiac and vascular issues", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                            }
                            Switch(
                                checked = preExistingConditions,
                                onCheckedChange = { preExistingConditions = it },
                                modifier = Modifier.testTag("pre_existing_switch")
                            )
                        }

                        if (selectedType == "HEALTH") {
                            // Coverage Level Tabs
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Coverage Comprehensive Tier", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf("Value", "Standard", "Premium").forEach { tier ->
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(
                                                    if (coverageLevel == tier) MaterialTheme.colorScheme.primaryContainer
                                                    else MaterialTheme.colorScheme.background
                                                )
                                                .border(
                                                    width = 1.dp,
                                                    color = if (coverageLevel == tier) MaterialTheme.colorScheme.primary
                                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                                                    shape = RoundedCornerShape(8.dp)
                                                )
                                                .clickable { coverageLevel = tier }
                                                .padding(vertical = 10.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                tier,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = if (coverageLevel == tier) MaterialTheme.colorScheme.onPrimaryContainer
                                                else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else if (selectedType == "VEHICLE") {
                        Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                        Text(
                            text = "Vehicle & Driving Records",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // Vehicle type dropdown simulator
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Vehicle Type Category", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                listOf("Sedan", "SUV", "Sports", "Bike").forEach { type ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (vehicleCategory == type) MaterialTheme.colorScheme.primaryContainer
                                                else MaterialTheme.colorScheme.background
                                            )
                                            .border(
                                                width = 1.dp,
                                                color = if (vehicleCategory == type) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable { vehicleCategory = type }
                                            .padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            type,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = if (vehicleCategory == type) MaterialTheme.colorScheme.onPrimaryContainer
                                            else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }

                        // Driving record selector
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Primary Driver Record", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(
                                    Triple("Clean", "Clean Slate", Icons.Default.CheckCircle),
                                    Triple("Minor", "Few tickets", Icons.Default.Warning),
                                    Triple("Major", "Risk profile", Icons.Default.Cancel)
                                ).forEach { record ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(
                                                if (drivingRecord == record.first) MaterialTheme.colorScheme.primaryContainer
                                                else MaterialTheme.colorScheme.background
                                            )
                                            .border(
                                                width = 1.dp,
                                                color = if (drivingRecord == record.first) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                                                shape = RoundedCornerShape(10.dp)
                                            )
                                            .clickable { drivingRecord = record.first }
                                            .padding(8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(
                                                imageVector = record.third,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp),
                                                tint = if (drivingRecord == record.first) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                record.first,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = if (drivingRecord == record.first) MaterialTheme.colorScheme.onPrimaryContainer
                                                else MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                record.second,
                                                fontSize = 9.sp,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryTabItem(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surface
            )
            .border(
                width = 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}
