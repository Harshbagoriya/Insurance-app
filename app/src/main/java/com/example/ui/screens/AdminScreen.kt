package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Claim
import com.example.data.model.InsurancePlan
import com.example.data.model.Policy
import com.example.data.model.UserAccount
import com.example.ui.theme.OceanPrimary
import com.example.ui.theme.OceanTertiary
import com.example.ui.theme.DangerRed
import com.example.ui.theme.WarningGold
import com.example.ui.viewmodel.InsuranceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    viewModel: InsuranceViewModel,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var adminTab by remember { mutableStateOf("CLAIMS") } // CLAIMS, PLANS, PATRONS
    var showCreatePlanDialog by remember { mutableStateOf(false) }
    var userToDelete by remember { mutableStateOf<UserAccount?>(null) }

    // Admin states from VM
    val claims by viewModel.adminAllClaims.collectAsStateWithLifecycle()
    val plans by viewModel.allPlans.collectAsStateWithLifecycle()
    val users by viewModel.adminAllUsers.collectAsStateWithLifecycle()
    val policies by viewModel.adminAllPolicies.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    // Calculate sum metrics
    val totalPremiums = remember(policies) { policies.sumOf { it.premiumAmount } }
    val pendingClaimsCount = remember(claims) { claims.count { it.status == "PENDING" } }
    val approvedClaimsTotal = remember(claims) { claims.filter { it.status == "APPROVED" }.sumOf { it.claimAmount } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Insurance App Admin Panel", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Company Operations Dashboard", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                    }
                },
                actions = {
                    IconButton(onClick = onLogout, modifier = Modifier.testTag("admin_logout_btn")) {
                        Icon(imageVector = Icons.Default.ExitToApp, contentDescription = "Log Out", tint = MaterialTheme.colorScheme.error)
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
        ) {
            // Metrics Row (Scrollable or simple symmetrical layout)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AdminMetricCard(
                    title = "Revenue",
                    value = "₹${String.format("%.0f", totalPremiums)}",
                    icon = Icons.Default.MonetizationOn,
                    backgroundColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    modifier = Modifier.weight(1f)
                )

                AdminMetricCard(
                    title = "Patrons",
                    value = "${users.size}",
                    icon = Icons.Default.Group,
                    backgroundColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                    modifier = Modifier.weight(1f)
                )

                AdminMetricCard(
                    title = "Pending",
                    value = "$pendingClaimsCount",
                    icon = Icons.Default.PendingActions,
                    backgroundColor = if (pendingClaimsCount > 0) WarningGold.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.weight(1f)
                )
            }

            // Tab bar switcher
            TabRow(
                selectedTabIndex = when (adminTab) {
                    "CLAIMS" -> 0
                    "PLANS" -> 1
                    "PATRONS" -> 2
                    else -> 3
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = adminTab == "CLAIMS",
                    onClick = { adminTab = "CLAIMS" },
                    modifier = Modifier.testTag("admin_tab_claims"),
                    text = { Text("Review Claims", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                )
                Tab(
                    selected = adminTab == "PLANS",
                    onClick = { adminTab = "PLANS" },
                    modifier = Modifier.testTag("admin_tab_plans"),
                    text = { Text("Configure Plans", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                )
                Tab(
                    selected = adminTab == "PATRONS",
                    onClick = { adminTab = "PATRONS" },
                    modifier = Modifier.testTag("admin_tab_patrons"),
                    text = { Text("Policies List", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                )
                Tab(
                    selected = adminTab == "USERS",
                    onClick = { adminTab = "USERS" },
                    modifier = Modifier.testTag("admin_tab_users"),
                    text = { Text("Users List", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                )
            }

            // Tab Contents
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp)
            ) {
                when (adminTab) {
                    "CLAIMS" -> {
                        if (claims.isEmpty()) {
                            AdminEmptyState(
                                icon = Icons.Default.Assignment,
                                message = "No claims filed yet in the system.",
                                hint = "Claims filed by customers will appear here in real-time."
                            )
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(claims, key = { it.id }) { claim ->
                                    AdminClaimCard(
                                        claim = claim,
                                        onApprove = { remarks -> viewModel.adminUpdateClaimStatus(claim, true, remarks) },
                                        onReject = { remarks -> viewModel.adminUpdateClaimStatus(claim, false, remarks) }
                                    )
                                }
                            }
                        }
                    }

                    "PLANS" -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { showCreatePlanDialog = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("admin_add_plan_btn"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Launch New Coverage Plan")
                            }

                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth().weight(1f)
                            ) {
                                items(plans, key = { it.id }) { plan ->
                                    AdminPlanCard(
                                        plan = plan,
                                        onDelete = { viewModel.adminDeletePlan(plan) }
                                    )
                                }
                            }
                        }
                    }

                    "PATRONS" -> {
                        if (policies.isEmpty()) {
                            AdminEmptyState(
                                icon = Icons.Default.BookmarkBorder,
                                message = "No active policies found.",
                                hint = "Once a customer buys a plan, it is registered here."
                            )
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(policies, key = { it.id }) { policy ->
                                    AdminPolicyListItem(policy = policy)
                                }
                            }
                        }
                    }

                    "USERS" -> {
                        if (users.isEmpty()) {
                            AdminEmptyState(
                                icon = Icons.Default.Group,
                                message = "No registered users found.",
                                hint = "Once a customer creates an account or signs in, they will appear here."
                            )
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(users, key = { it.email }) { user ->
                                    val userPoliciesCount = policies.count { it.userEmail == user.email }
                                    val userClaimsCount = claims.count { it.userEmail == user.email }
                                    AdminUserListItem(
                                        user = user,
                                        policiesCount = userPoliciesCount,
                                        claimsCount = userClaimsCount,
                                        showDeleteButton = user.email != (currentUser?.email ?: ""),
                                        onDelete = { userToDelete = user }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal dialogue to create plans
    if (showCreatePlanDialog) {
        CreatePlanDialog(
            onDismiss = { showCreatePlanDialog = false },
            onCreate = { title, type, desc, basePremium, coverageAmt, features ->
                viewModel.adminCreatePlan(title, type, desc, basePremium, coverageAmt, features)
                showCreatePlanDialog = false
            }
        )
    }

    // Modal dialogue to confirm user deletion
    if (userToDelete != null) {
        AlertDialog(
            onDismissRequest = { userToDelete = null },
            title = { Text("Delete User Account") },
            text = { Text("Are you sure you want to delete account of ${userToDelete?.name} (${userToDelete?.email})? This will also remove all their registered policies, pending or processed claims, and associated reminders.") },
            confirmButton = {
                Button(
                    onClick = {
                        userToDelete?.email?.let { email ->
                            viewModel.adminDeleteUser(email)
                        }
                        userToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("confirm_delete_user_btn")
                ) {
                    Text("Delete Permanently")
                }
            },
            dismissButton = {
                TextButton(onClick = { userToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun AdminMetricCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
            Text(text = title, fontSize = 10.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f))
        }
    }
}

@Composable
fun AdminEmptyState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    message: String,
    hint: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = message, fontWeight = FontWeight.Bold, fontSize = 16.sp, textAlign = TextAlign.Center)
        Text(
            text = hint,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun AdminClaimCard(
    claim: Claim,
    onApprove: (String) -> Unit,
    onReject: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var remarksText by remember { mutableStateOf("") }
    var expandReview by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "Claim for ${claim.planTitle}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Policy No: ${claim.policyNumber} • Client: ${claim.userEmail}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }

                // Badge Status
                Surface(
                    color = when (claim.status) {
                        "PENDING" -> WarningGold.copy(alpha = 0.15f)
                        "APPROVED" -> OceanTertiary.copy(alpha = 0.15f)
                        else -> DangerRed.copy(alpha = 0.1f)
                    },
                    shape = RoundedCornerShape(8.dp),
                    contentColor = when (claim.status) {
                        "PENDING" -> WarningGold
                        "APPROVED" -> OceanTertiary
                        else -> DangerRed
                    }
                ) {
                    Text(
                        text = claim.status,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))

            // Body reasoning info
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Requested Amount:", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Text("₹${String.format("%.2f", claim.claimAmount)}", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
                Text("Reason for claim:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                Text(
                    text = claim.reason,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                )
            }

            if (claim.status == "PENDING") {
                if (!expandReview) {
                    Button(
                        onClick = { expandReview = true },
                        modifier = Modifier.fillMaxWidth().testTag("review_claim_trigger_${claim.id}"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
                    ) {
                        Text("Initiate Security Review")
                    }
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    ) {
                        OutlinedTextField(
                            value = remarksText,
                            onValueChange = { remarksText = it },
                            label = { Text("Appraisal Assessment Note / Remarks") },
                            placeholder = { Text("State reasons for approval/rejection...") },
                            modifier = Modifier.fillMaxWidth().testTag("reviewer_remarks_${claim.id}"),
                            maxLines = 2,
                            shape = RoundedCornerShape(10.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { onApprove(remarksText) },
                                modifier = Modifier.weight(1f).testTag("claim_approve_btn_${claim.id}"),
                                colors = ButtonDefaults.buttonColors(containerColor = OceanTertiary),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Approve Pay")
                            }

                            Button(
                                onClick = { onReject(remarksText) },
                                modifier = Modifier.weight(1f).testTag("claim_reject_btn_${claim.id}"),
                                colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Reject Claim")
                            }
                        }
                    }
                }
            } else {
                // Done status remarks review
                claim.remarks?.let {
                    if (it.isNotEmpty()) {
                        Text(
                            text = "Admin appraisal note: $it",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdminPlanCard(
    plan: InsurancePlan,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = plan.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape
                    ) {
                        Text(
                            text = plan.type,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = plan.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Base premium: ₹${plan.basePremium}/mo • Coverage: ₹${String.format("%,.0f", plan.coverageAmount)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            IconButton(onClick = onDelete, modifier = Modifier.testTag("delete_plan_btn_${plan.id}")) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Plan", tint = DangerRed)
            }
        }
    }
}

@Composable
fun AdminPolicyListItem(
    policy: Policy,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = policy.planTitle,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = policy.status,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = if (policy.status == "ACTIVE") OceanTertiary else WarningGold
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Holder Email: ${policy.userEmail}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            Text(text = "Policy Code: ${policy.policyNumber} | Age: ${policy.insuredAge} | Premium: ₹${policy.premiumAmount}/mo", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }
    }
}

@Composable
fun CreatePlanDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String, String, Double, Double, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("HEALTH") } // HEALTH, VEHICLE, LIFE, HOME
    var description by remember { mutableStateOf("") }
    var basePremium by remember { mutableStateOf("") }
    var coverageAmount by remember { mutableStateOf("") }
    var features by remember { mutableStateOf("") }

    var errorsFlag by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Launch Brand New Plan", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Plan Title") },
                    modifier = Modifier.fillMaxWidth().testTag("create_plan_title_input"),
                    singleLine = true
                )

                // Type Option select
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Plan Category Type", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("HEALTH", "VEHICLE", "LIFE", "HOME").forEach { t ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (type == t) MaterialTheme.colorScheme.primaryContainer
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                    )
                                    .clickable { type = t }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(t, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (type == t) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Brief Description") },
                    modifier = Modifier.fillMaxWidth().testTag("create_plan_desc_input"),
                    maxLines = 2
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = basePremium,
                        onValueChange = { basePremium = it },
                        label = { Text("Base ₹/mo") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag("create_plan_premium_input"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = coverageAmount,
                        onValueChange = { coverageAmount = it },
                        label = { Text("Coverage Limit ₹") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag("create_plan_coverage_input"),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = features,
                    onValueChange = { features = it },
                    label = { Text("Key Features (comma list)") },
                    placeholder = { Text("No copay, 24/7 towing...") },
                    modifier = Modifier.fillMaxWidth().testTag("create_plan_features_input")
                )

                if (errorsFlag) {
                    Text(
                        "Please fill out all fields with valid numbers",
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
                    val premiumNum = basePremium.toDoubleOrNull()
                    val coverageNum = coverageAmount.toDoubleOrNull()
                    if (title.isNotEmpty() && description.isNotEmpty() && premiumNum != null && coverageNum != null && features.isNotEmpty()) {
                        onCreate(title, type, description, premiumNum, coverageNum, features)
                    } else {
                        errorsFlag = true
                    }
                },
                modifier = Modifier.testTag("admin_submit_plan_btn")
            ) {
                Text("Launch Plan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AdminUserListItem(
    user: UserAccount,
    policiesCount: Int,
    claimsCount: Int,
    showDeleteButton: Boolean,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // User Avatar/Initial Circle
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = (user.name.takeIf { it.isNotEmpty() }?.first() ?: 'U').uppercase().toString(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = user.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    // Badge for User Role (Manager/Customer)
                    Surface(
                        color = if (user.role == "ADMIN") MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (user.role == "ADMIN") "Manager" else "Customer",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (user.role == "ADMIN") MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = user.email,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Policies tag
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                modifier = Modifier.size(10.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$policiesCount Policies",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Claims tag
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PendingActions,
                                contentDescription = null,
                                modifier = Modifier.size(10.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$claimsCount Claims",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            if (showDeleteButton) {
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.testTag("admin_delete_user_${user.email}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete User Account",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
