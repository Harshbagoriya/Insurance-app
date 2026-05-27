package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Claim
import com.example.data.model.InsurancePlan
import com.example.data.model.Policy
import com.example.data.model.Reminder
import com.example.ui.theme.*
import com.example.ui.viewmodel.InsuranceViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: InsuranceViewModel,
    onNavigateToCalculator: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val policies by viewModel.userPolicies.collectAsStateWithLifecycle()
    val claims by viewModel.userClaims.collectAsStateWithLifecycle()
    val reminders by viewModel.userReminders.collectAsStateWithLifecycle()
    val plans by viewModel.allPlans.collectAsStateWithLifecycle()

    var showReminderPanel by remember { mutableStateOf(false) }
    var selectedPlanForPurchase by remember { mutableStateOf<InsurancePlan?>(null) }
    var runningClaimPolicyTarget by remember { mutableStateOf<Policy?>(null) }
    var selectedCategoryFilter by remember { mutableStateOf("ALL") }

    val unreadRemindersCount = remember(reminders) { reminders.count { !it.isRead } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (currentUser?.name ?: "U").take(1).uppercase(),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = currentUser?.name ?: "Valued Customer",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = currentUser?.email ?: "shield@secure.com",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                },
                actions = {
                    // Calculator Shortcut Icon
                    IconButton(onClick = onNavigateToCalculator, modifier = Modifier.testTag("dashboard_calc_icon_btn")) {
                        Icon(imageVector = Icons.Default.Calculate, contentDescription = "Premium Estimator", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    }

                    // Notification Bell Badge
                    Box(contentAlignment = Alignment.TopEnd) {
                        IconButton(onClick = { showReminderPanel = !showReminderPanel }, modifier = Modifier.testTag("bell_icon_btn")) {
                            Icon(imageVector = Icons.Outlined.Notifications, contentDescription = "Reminders", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                        if (unreadRemindersCount > 0) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 8.dp, end = 8.dp)
                                    .size(16.dp)
                                    .background(Color.Red, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = unreadRemindersCount.toString(),
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Logout Icon
                    IconButton(onClick = onLogout, modifier = Modifier.testTag("logout_btn")) {
                        Icon(imageVector = Icons.Default.Logout, contentDescription = "Log Out", tint = MaterialTheme.colorScheme.error)
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
            // Unread Alert reminders dropdown inline
            AnimatedVisibility(
                visible = showReminderPanel || unreadRemindersCount > 0,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            RoundedCornerShape(16.dp)
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.12f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Smart Alerts & Reminders",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            if (reminders.isNotEmpty()) {
                                TextButton(
                                    onClick = { viewModel.clearAllReminders() },
                                    modifier = Modifier.testTag("clear_all_reminders_btn")
                                ) {
                                    Text("Clear All", fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }

                        if (reminders.isEmpty()) {
                            Text(
                                text = "Inbox clean! No active updates or renewals pending right now.",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        } else {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                reminders.take(4).forEach { reminder ->
                                    ReminderAlertItem(
                                        reminder = reminder,
                                        onDismiss = { viewModel.clearReminder(reminder.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Quick Simulator Trigger Drawer (For Testing payment, update, renewals)
            if (policies.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "🛠️ Developer Testing simulation sandbox",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.triggerRenewalPreviewReminder(policies.first()) },
                                modifier = Modifier.weight(1f).testTag("simulate_renewal_btn"),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                            ) {
                                Text("Simulate Renewal ⏰", fontSize = 10.sp)
                            }
                            Button(
                                onClick = { viewModel.triggerPaymentReminder(policies.first()) },
                                modifier = Modifier.weight(1f).testTag("simulate_payment_btn"),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                            ) {
                                Text("Simulate Payment 💳", fontSize = 10.sp)
                            }
                        }
                        Text(
                            "Click triggers to populate the smart dynamic alert panel with instant renewal or payment items.",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            // Policybazaar Inspired Trust Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = WarningGold,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "India's Trusted Aggregator Partner",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 0.3.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Compare & Buy Best Insurance Policies",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "Save up to 40% on top plans • Cashless claims at 10,000+ Indian network hospitals",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Interactive Category grid of Policybazaar
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Quick Insurance Categories",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                )

                // Row Grid of categories
                val categories = listOf(
                    Triple("ALL", "All Covers", Icons.Default.List),
                    Triple("HEALTH", "Health", Icons.Default.Security),
                    Triple("VEHICLE", "Vehicle", Icons.Default.DirectionsCar),
                    Triple("LIFE", "Life Term", Icons.Default.Favorite),
                    Triple("HOME", "Home Security", Icons.Default.Home)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    items(categories) { (catKey, label, icon) ->
                        val isSelected = selectedCategoryFilter == catKey
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                            ),
                            modifier = Modifier
                                .clickable { selectedCategoryFilter = catKey }
                                .testTag("cat_filter_$catKey")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    tint = if (isSelected) Color.White else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // 1. Interactive Policies Secure Engine View
            Text(
                text = "Real-time Policy Security Vault",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            if (policies.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                            modifier = Modifier.size(56.dp)
                        )
                        Text(
                            text = "No Active Policies Secured",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Choose a cover blueprint from our active plans below and activate instant coverage in 60 seconds.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    policies.forEach { policy ->
                        ActivePolicyCard(
                            policy = policy,
                            onFileClaimTrigger = { runningClaimPolicyTarget = policy }
                        )
                    }
                }
            }

            // 2. Active Claims History tracker
            if (claims.isNotEmpty()) {
                Text(
                    text = "Claim Settlement Dashboard",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    claims.forEach { claim ->
                        ClaimStatusMiniCard(claim = claim)
                    }
                }
            }

            // 3. Purchase Covers blue-prints catalog lists
            Text(
                text = if (selectedCategoryFilter == "ALL") "Explore Coverage Categories" else "Best ${selectedCategoryFilter.lowercase().replaceFirstChar { it.uppercase() }} Blueprints Available",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            plans.filter { !it.isCustom && (selectedCategoryFilter == "ALL" || it.type == selectedCategoryFilter) }.let { stdPlans ->
                if (stdPlans.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No active blueprints in this category.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        stdPlans.forEach { plan ->
                            PlanCatalogCard(
                                plan = plan,
                                onConfigureAndBuy = { selectedPlanForPurchase = plan }
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal buy dialog with dynamic calculator
    selectedPlanForPurchase?.let { plan ->
        PurchaseCoverageDialog(
            plan = plan,
            viewModel = viewModel,
            onDismiss = { selectedPlanForPurchase = null },
            onPurchaseConfirmed = { age, details, finalPrice ->
                viewModel.buyPolicy(plan, age, details, finalPrice)
                selectedPlanForPurchase = null
            }
        )
    }

    // Modal filing claims drawer
    runningClaimPolicyTarget?.let { policy ->
        FileClaimOnlineDialog(
            policy = policy,
            onDismiss = { runningClaimPolicyTarget = null },
            onSubmitClaim = { amount, reason ->
                viewModel.fileClaim(policy, amount, reason)
                runningClaimPolicyTarget = null
            }
        )
    }
}

@Composable
fun ReminderAlertItem(
    reminder: Reminder,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            1.dp,
            when (reminder.type) {
                "RENEWAL" -> WarningGold.copy(alpha = 0.2f)
                "CLAIM_UPDATE" -> OceanSecondary.copy(alpha = 0.15f)
                else -> OceanTertiary.copy(alpha = 0.15f)
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        color = when (reminder.type) {
                            "RENEWAL" -> WarningGold.copy(alpha = 0.15f)
                            "CLAIM_UPDATE" -> OceanSecondary.copy(alpha = 0.12f)
                            else -> OceanTertiary.copy(alpha = 0.12f)
                        },
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (reminder.type) {
                        "RENEWAL" -> Icons.Default.AccessTime
                        "CLAIM_UPDATE" -> Icons.Default.Done
                        else -> Icons.Default.Star
                    },
                    contentDescription = null,
                    tint = when (reminder.type) {
                        "RENEWAL" -> WarningGold
                        "CLAIM_UPDATE" -> OceanSecondary
                        else -> OceanTertiary
                    },
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = reminder.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = reminder.message,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(24.dp).testTag("dismiss_reminder_${reminder.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
fun ActivePolicyCard(
    policy: Policy,
    onFileClaimTrigger: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formatter = remember { SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()) }
    val endFormatted = remember(policy.endDate) { formatter.format(Date(policy.endDate)) }

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = policy.planTitle,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "SECURE POLICY ID: ${policy.policyNumber}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        letterSpacing = 0.5.sp
                    )
                }

                Surface(
                    color = if (policy.status == "ACTIVE") OceanTertiary.copy(alpha = 0.15f) else WarningGold.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp),
                    contentColor = if (policy.status == "ACTIVE") OceanTertiary else WarningGold
                ) {
                    Text(
                        text = policy.status,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Premium detail
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Guaranteed coverage limit", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    Text(
                        text = if (policy.type == "HEALTH") "₹1,000,000" else "₹500,000",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Monthly premium rate", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    Text(
                        text = "₹${String.format("%.2f", policy.premiumAmount)}/mo",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Meta indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Event,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Renews: $endFormatted",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                Button(
                    onClick = onFileClaimTrigger,
                    modifier = Modifier.testTag("policy_file_claim_btn_${policy.id}"),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(imageVector = Icons.Default.NoteAdd, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("File Claim Online", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ClaimStatusMiniCard(
    claim: Claim,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.HistoryEdu,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Claim for ${claim.planTitle}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Text(
                    text = "Amount: ₹${String.format("%.2f", claim.claimAmount)} • ID: ${claim.policyNumber}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }

            Surface(
                color = when (claim.status) {
                    "PENDING" -> WarningGold.copy(alpha = 0.15f)
                    "APPROVED" -> OceanTertiary.copy(alpha = 0.15f)
                    else -> DangerRed.copy(alpha = 0.1f)
                },
                contentColor = when (claim.status) {
                    "PENDING" -> WarningGold
                    "APPROVED" -> OceanTertiary
                    else -> DangerRed
                },
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = claim.status,
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }
    }
}

@Composable
fun PlanCatalogCard(
    plan: InsurancePlan,
    onConfigureAndBuy: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 4.dp)) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (plan.type) {
                                    "HEALTH" -> Icons.Default.Security
                                    "VEHICLE" -> Icons.Default.DirectionsCar
                                    "LIFE" -> Icons.Default.Favorite
                                    else -> Icons.Default.Home
                                },
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = plan.title,
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Text(
                        text = plan.description,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "From",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "₹${plan.basePremium}",
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "/month",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))

            Spacer(modifier = Modifier.height(8.dp))

            // Horizontal pill list of features
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(plan.features.split(",")) { feature ->
                    Surface(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                    ) {
                        Text(
                            text = feature.trim(),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onConfigureAndBuy,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("buy_plan_btn_${plan.id}"),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Configure & Buy Policy online")
            }
        }
    }
}

// Dialog for filing claims
@Composable
fun FileClaimOnlineDialog(
    policy: Policy,
    onDismiss: () -> Unit,
    onSubmitClaim: (Double, String) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }
    var errorFlag by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Online Settlement Request", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Request claims filing under policy '${policy.planTitle}' (${policy.policyNumber}) quickly safely.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Requested Claim Compensation Amount (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("claim_amount_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Incident Reason / Statement") },
                    placeholder = { Text("Detailed account of when and how damage or healthcare occurred...") },
                    modifier = Modifier.fillMaxWidth().testTag("claim_reason_input"),
                    maxLines = 3
                )

                if (errorFlag) {
                    Text(
                        "Please fill out valid compensation amount and incident statement notes",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val dVal = amount.toDoubleOrNull()
                    if (dVal != null && dVal > 0 && reason.isNotBlank()) {
                        onSubmitClaim(dVal, reason)
                    } else {
                        errorFlag = true
                    }
                },
                modifier = Modifier.testTag("claim_submit_btn")
            ) {
                Text("File Online Claim")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Interactive Purchase coverage modal integrating custom risk modifiers in premium calculations
@Composable
fun PurchaseCoverageDialog(
    plan: InsurancePlan,
    viewModel: InsuranceViewModel,
    onDismiss: () -> Unit,
    onPurchaseConfirmed: (Int, String, Double) -> Unit
) {
    var ageInp by remember { mutableStateOf(30f) }
    var tobaccoUser by remember { mutableStateOf(false) }
    var preExistingConditions by remember { mutableStateOf(false) }
    var coverageLevel by remember { mutableStateOf("Standard") } // Value, Standard, Premium

    // Vehicle specific elements
    var vehicleCategory by remember { mutableStateOf("Sedan") }
    var drivingRecord by remember { mutableStateOf("Clean") }

    val liveComputedRate = remember(ageInp, tobaccoUser, preExistingConditions, coverageLevel, vehicleCategory, drivingRecord) {
        viewModel.calculateEstimatedPremium(
            plan = plan,
            age = ageInp.toInt(),
            tobaccoUser = tobaccoUser,
            preExistingConditions = preExistingConditions,
            coverageLevel = coverageLevel,
            vehicleType = vehicleCategory,
            drivingRecord = drivingRecord
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Configure Dynamic Blueprint", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header details
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(plan.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Base cover: ₹${plan.basePremium}/mo", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                        }
                        Box(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                "₹${String.format("%.2f", liveComputedRate)}",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                // Spacing age configuration
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Accredited Applicant Age", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                        Text("${ageInp.toInt()} Years", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
                    }
                    Slider(
                        value = ageInp,
                        onValueChange = { ageInp = it },
                        valueRange = 18f..85f,
                        modifier = Modifier.testTag("p_age_slider")
                    )
                }

                // Category-specific configuration fields
                if (plan.type == "HEALTH" || plan.type == "LIFE") {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Personal Habits Statement", fontWeight = FontWeight.Bold, fontSize = 13.sp)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Tobacco/Nicotine User (+45% risk loading)", fontSize = 12.sp)
                            Switch(checked = tobaccoUser, onCheckedChange = { tobaccoUser = it }, modifier = Modifier.testTag("p_tobacco_switch"))
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Pre-existing Medical History (+35%)", fontSize = 12.sp)
                            Switch(checked = preExistingConditions, onCheckedChange = { preExistingConditions = it }, modifier = Modifier.testTag("p_pre_switch"))
                        }

                        if (plan.type == "HEALTH") {
                            Text("Comprehensive Protection Tier", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf("Value", "Standard", "Premium").forEach { tier ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (coverageLevel == tier) MaterialTheme.colorScheme.primaryContainer
                                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                            )
                                            .clickable { coverageLevel = tier }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(tier, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (coverageLevel == tier) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                                    }
                                }
                            }
                        }
                    }
                } else if (plan.type == "VEHICLE") {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Vehicle Profile Category", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("Sedan", "SUV", "Sports", "Bike").forEach { type ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (vehicleCategory == type) MaterialTheme.colorScheme.primaryContainer
                                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                        )
                                        .clickable { vehicleCategory = type }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(type, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (vehicleCategory == type) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }

                        Text("Driver Safety Rating Record", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("Clean", "Minor Violations", "Major Offenses").forEach { record ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (drivingRecord == record) MaterialTheme.colorScheme.primaryContainer
                                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                        )
                                        .clickable { drivingRecord = record }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(record.split(" ").first(), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (drivingRecord == record) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val descriptor = when (plan.type) {
                        "HEALTH" -> "Tobacco: $tobaccoUser, History: $preExistingConditions, Comp: $coverageLevel"
                        "VEHICLE" -> "Vehicle Type: $vehicleCategory, Record: $drivingRecord"
                        else -> "Age: ${ageInp.toInt()} yrs, Profile Confirmed"
                    }
                    onPurchaseConfirmed(ageInp.toInt(), descriptor, liveComputedRate)
                },
                modifier = Modifier.testTag("complete_purchase_dialog_btn")
            ) {
                Text("Confirm & Secure Policy")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Secure and optimized local assets mapping
