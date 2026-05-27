package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.composable
import com.example.ui.screens.AdminScreen
import com.example.ui.screens.CalculatorScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.InsuranceViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    
    setContent {
      MyApplicationTheme {
        val navController = rememberNavController()
        val viewModel: InsuranceViewModel = viewModel()
        val currentUser = viewModel.currentUser.collectAsState()

        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
          NavHost(
            navController = navController,
            startDestination = "auth",
            modifier = Modifier.padding(innerPadding)
          ) {
            
            // 1. Unified Auth Gateway Screen
            composable("auth") {
              LoginScreen(
                onLoginSuccess = { email, name, role ->
                  viewModel.login(email, name, role)
                  if (role == "ADMIN") {
                    navController.navigate("admin") {
                      popUpTo("auth") { inclusive = true }
                    }
                  } else {
                    navController.navigate("dashboard") {
                      popUpTo("auth") { inclusive = true }
                    }
                  }
                }
              )
            }

            // 2. Secured User Dashboard Panel
            composable("dashboard") {
              DashboardScreen(
                viewModel = viewModel,
                onNavigateToCalculator = {
                  navController.navigate("calculator")
                },
                onLogout = {
                  viewModel.logout()
                  navController.navigate("auth") {
                    popUpTo("dashboard") { inclusive = true }
                  }
                }
              )
            }

            // 3. Admin & Manager Control Center
            composable("admin") {
              AdminScreen(
                viewModel = viewModel,
                onLogout = {
                  viewModel.logout()
                  navController.navigate("auth") {
                    popUpTo("admin") { inclusive = true }
                  }
                }
              )
            }

            // 4. Standalone Comprehensive Estimator
            composable("calculator") {
              CalculatorScreen(
                onNavigateBack = {
                  navController.popBackStack()
                }
              )
            }
          }
        }
      }
    }
  }
}
