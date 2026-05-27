package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
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
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.OceanPrimary
import com.example.ui.theme.OceanSecondary
import com.example.ui.theme.WarningGold

@Composable
fun LoginScreen(
    onLoginSuccess: (email: String, name: String, role: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showGoogleModal by remember { mutableStateOf(false) }
    var selectedRole by remember { mutableStateOf("USER") } // USER vs ADMIN
    var isSignUpMode by remember { mutableStateOf(false) }
    var inputName by remember { mutableStateOf("") }
    var inputEmail by remember { mutableStateOf("") }
    var formError by remember { mutableStateOf<String?>(null) }

    val gradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.82f),
            MaterialTheme.colorScheme.background
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(gradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .statusBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header spacing
            Spacer(modifier = Modifier.height(20.dp))

            // Brand Section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.2f),
                            shape = CircleShape
                        )
                        .border(2.dp, MaterialTheme.colorScheme.onPrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Insurance Logo",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(56.dp)
                    )
                }

                Text(
                    text = "Insurance App",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "A flexible, high-trust digital workspace designed for claim transparency, live active policy tracking, and automated renewals.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // Interactive Role Select and Sign In Button
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Access Workspace",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Role Tabs Selector
                    if (!isSignUpMode) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Button(
                                onClick = { selectedRole = "USER" },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("select_user_role_btn"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selectedRole == "USER") MaterialTheme.colorScheme.primary else Color.Transparent,
                                    contentColor = if (selectedRole == "USER") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                ),
                                contentPadding = PaddingValues(vertical = 12.dp)
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Customer")
                            }

                            Button(
                                onClick = { selectedRole = "ADMIN" },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("select_admin_role_btn"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selectedRole == "ADMIN") MaterialTheme.colorScheme.primary else Color.Transparent,
                                    contentColor = if (selectedRole == "ADMIN") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                ),
                                contentPadding = PaddingValues(vertical = 12.dp)
                            ) {
                                Icon(Icons.Default.AdminPanelSettings, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Manager")
                            }
                        }
                    } else {
                        // Helpful feedback that only Customer role accounts are self-creatable
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Creating Customer Account",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }

                    // Mode Toggle (Sign In vs Create Account Switcher)
                    TabRow(
                        selectedTabIndex = if (isSignUpMode) 1 else 0,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        containerColor = Color.Transparent,
                        divider = {}
                    ) {
                        Tab(
                            selected = !isSignUpMode,
                            onClick = { isSignUpMode = false; formError = null },
                            modifier = Modifier.testTag("signin_tab"),
                            text = { Text("Sign In", fontWeight = FontWeight.SemiBold) }
                        )
                        Tab(
                            selected = isSignUpMode,
                            onClick = { isSignUpMode = true; selectedRole = "USER"; formError = null },
                            modifier = Modifier.testTag("signup_tab"),
                            text = { Text("Create Account", fontWeight = FontWeight.SemiBold) }
                        )
                    }

                    if (isSignUpMode) {
                        // Custom Create Account / Registration Form
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = inputName,
                                onValueChange = { inputName = it; formError = null },
                                label = { Text("Full Name") },
                                modifier = Modifier.fillMaxWidth().testTag("signup_name_input"),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )

                            OutlinedTextField(
                                value = inputEmail,
                                onValueChange = { inputEmail = it; formError = null },
                                label = { Text("Email Address") },
                                modifier = Modifier.fillMaxWidth().testTag("signup_email_input"),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )

                            if (formError != null) {
                                Text(
                                    text = formError!!,
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )
                            }

                            Button(
                                onClick = {
                                    val trimmedName = inputName.trim()
                                    val trimmedEmail = inputEmail.trim()
                                    if (trimmedName.isEmpty() || trimmedEmail.isEmpty()) {
                                        formError = "Please enter both full name and email address."
                                    } else if (!trimmedEmail.contains("@") || !trimmedEmail.contains(".")) {
                                        formError = "Please enter a valid email address."
                                    } else {
                                        onLoginSuccess(trimmedEmail, trimmedName, "USER")
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(50.dp).testTag("signup_submit_btn"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Register & Launch Workspace", fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        // Sign In Options
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Quick / Custom Login by typing Email
                            OutlinedTextField(
                                value = inputEmail,
                                onValueChange = { inputEmail = it; formError = null },
                                label = { Text("Registered Email") },
                                placeholder = { Text("e.g., guest.user@insurance.com") },
                                modifier = Modifier.fillMaxWidth().testTag("signin_email_input"),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )

                            if (formError != null) {
                                Text(
                                    text = formError!!,
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 12.sp
                                )
                            }

                            Button(
                                onClick = {
                                    val trimmedEmail = inputEmail.trim()
                                    if (trimmedEmail.isEmpty()) {
                                        formError = "Please enter your email."
                                    } else if (!trimmedEmail.contains("@")) {
                                        formError = "Please enter a valid email."
                                    } else {
                                        // Auto-resolve a name or seed as Guest
                                        val assumedName = trimmedEmail.substringBefore("@")
                                            .replace(".", " ")
                                            .replaceFirstChar { it.uppercase() }
                                        onLoginSuccess(trimmedEmail, assumedName, selectedRole)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(50.dp).testTag("signin_quick_btn"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Express Login")
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                                Text("  or select account  ", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                                HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                            }

                            // Simulated Google Login trigger
                            Button(
                                onClick = { showGoogleModal = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("google_signin_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Box(modifier = Modifier.size(20.dp), contentAlignment = Alignment.Center) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                            Box(modifier = Modifier.size(4.dp).background(Color(0xFFEA4335), CircleShape))
                                            Box(modifier = Modifier.size(4.dp).background(Color(0xFF4285F4), CircleShape))
                                            Box(modifier = Modifier.size(4.dp).background(Color(0xFFFBBC05), CircleShape))
                                            Box(modifier = Modifier.size(4.dp).background(Color(0xFF34A853), CircleShape))
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Sign in with Google",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Simulated authentic Google Single-Sign-On Account Chooser Modal Dialog
    if (showGoogleModal) {
        Dialog(onDismissRequest = { showGoogleModal = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Google Branding Icon
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "G",
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp,
                            color = Color(0xFF4285F4)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Sign in with Google to Insurance App",
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

                    Text(
                        text = "Choose an account to continue",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Normal,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    // Profile options list
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (selectedRole == "USER") {
                            // Demo customer profile
                            GoogleProfileItem(
                                name = "Guest User",
                                email = "guest.user@insurance.com",
                                initial = 'G',
                                avatarBg = Color(0xFF34A853),
                                onClick = {
                                    showGoogleModal = false
                                    onLoginSuccess("guest.user@insurance.com", "Guest User", "USER")
                                }
                            )
                        } else {
                            // Demo admin profile
                            GoogleProfileItem(
                                name = "Admin Manager",
                                email = "admin@insurance.com",
                                initial = 'A',
                                avatarBg = Color(0xFFEA4335),
                                onClick = {
                                    showGoogleModal = false
                                    onLoginSuccess("admin@insurance.com", "Admin Manager", "ADMIN")
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "By continuing, Google shares your profile image and email with Insurance App's secure policy engine.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center
                    )

                    TextButton(
                        onClick = { showGoogleModal = false },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Cancel", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@Composable
fun GoogleProfileItem(
    name: String,
    email: String,
    initial: Char,
    avatarBg: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .border(
                1.dp,
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                RoundedCornerShape(12.dp)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Circle Avatar
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(avatarBg, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initial.toString(),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = email,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }

        Icon(
            imageVector = Icons.Default.ArrowForward,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            modifier = Modifier.size(16.dp)
        )
    }
}
