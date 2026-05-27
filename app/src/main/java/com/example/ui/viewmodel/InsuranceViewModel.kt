package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.*
import com.example.data.repository.InsuranceRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class InsuranceViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: InsuranceRepository

    // Current User State
    private val _currentUser = MutableStateFlow<UserAccount?>(null)
    val currentUser: StateFlow<UserAccount?> = _currentUser.asStateFlow()

    // Seeds Database on Startup
    init {
        val db = AppDatabase.getDatabase(application)
        repository = InsuranceRepository(db.insuranceDao())
        viewModelScope.launch {
            repository.seedDatabaseIfEmpty()
            
            // Log in as a sample user initially to provide an instant interactive workspace,
            // while allowing users to create accounts, sign out, and switch roles easily.
            val guestEmail = "guest.user@insurance.com"
            var guest = repository.getUserByEmail(guestEmail)
            if (guest == null) {
                guest = UserAccount(guestEmail, "Guest User", "USER")
                repository.insertUser(guest)
            }
            _currentUser.value = guest
        }
    }

    // Dynamic Lists based on current user session
    val allPlans: StateFlow<List<InsurancePlan>> = repository.getAllPlansFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val userPolicies: StateFlow<List<Policy>> = _currentUser
        .filterNotNull()
        .flatMapLatest { user -> repository.getPoliciesByUserFlow(user.email) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val userClaims: StateFlow<List<Claim>> = _currentUser
        .filterNotNull()
        .flatMapLatest { user -> repository.getClaimsByUserFlow(user.email) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val userReminders: StateFlow<List<Reminder>> = _currentUser
        .filterNotNull()
        .flatMapLatest { user -> repository.getRemindersByUserFlow(user.email) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Admin Specific States
    val adminAllUsers: StateFlow<List<UserAccount>> = repository.getAllUsersFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val adminAllPolicies: StateFlow<List<Policy>> = repository.getAllPoliciesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val adminAllClaims: StateFlow<List<Claim>> = repository.getAllClaimsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // AUTH ACTIONS
    fun login(email: String, name: String, role: String) {
        viewModelScope.launch {
            var user = repository.getUserByEmail(email)
            if (user == null || user.role != role) {
                user = UserAccount(email, name, role)
                repository.insertUser(user)
            }
            _currentUser.value = user
        }
    }

    fun logout() {
        _currentUser.value = null
    }

    // POLICY ACTIONS
    fun buyPolicy(
        plan: InsurancePlan,
        insuredAge: Int,
        coverageNeeds: String,
        calculatedPremium: Double
    ) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val shortId = UUID.randomUUID().toString().take(6).uppercase()
            val policyNo = "POL-${plan.type.take(3)}-${shortId}"
            val now = System.currentTimeMillis()
            val oneYearFromNow = now + (365L * 24 * 60 * 60 * 1000)

            val newPolicy = Policy(
                policyNumber = policyNo,
                userEmail = user.email,
                planId = plan.id,
                planTitle = plan.title,
                type = plan.type,
                insuredAge = insuredAge,
                coverageNeeds = coverageNeeds,
                premiumAmount = calculatedPremium,
                startDate = now,
                endDate = oneYearFromNow,
                status = "ACTIVE"
            )

            repository.insertPolicy(newPolicy)

            // Trigger active payment confirm / reminder logs automatically for interactive realism
            val paymentConf = Reminder(
                userEmail = user.email,
                title = "Payment Successful 🎉",
                message = "Your premium payment of ₹${String.format("%.2f", calculatedPremium)} for '${plan.title}' of policy ($policyNo) is confirmed! Policy status is active.",
                type = "PAYMENT"
            )
            repository.insertReminder(paymentConf)
        }
    }

    // FILE A CLAIM
    fun fileClaim(policy: Policy, claimAmount: Double, reason: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val claim = Claim(
                policyId = policy.id,
                policyNumber = policy.policyNumber,
                planTitle = policy.planTitle,
                userEmail = user.email,
                claimAmount = claimAmount,
                reason = reason,
                status = "PENDING",
                filedDate = System.currentTimeMillis()
            )
            repository.insertClaim(claim)

            // Notify claimant of successful submission
            val claimReceipt = Reminder(
                userEmail = user.email,
                title = "Claim Filed: ${policy.policyNumber} 📄",
                message = "Your claim for ₹${String.format("%.2f", claimAmount)} under plan '${policy.planTitle}' has been filed and sent to our admin panel for review.",
                type = "CLAIM_UPDATE"
            )
            repository.insertReminder(claimReceipt)
        }
    }

    // REMINDERS AND SIMULATION CONTROL
    fun clearReminder(reminderId: Int) {
        viewModelScope.launch {
            repository.deleteReminderById(reminderId)
        }
    }

    fun clearAllReminders() {
        val email = _currentUser.value?.email ?: return
        viewModelScope.launch {
            repository.clearAllRemindersForUser(email)
        }
    }

    // Simulate instant reminder triggers for testing reminders
    fun triggerRenewalPreviewReminder(policy: Policy) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val reminder = Reminder(
                userEmail = user.email,
                title = "⏰ Policy Renewal Due soon",
                message = "Plan '${policy.planTitle}' (${policy.policyNumber}) is due for renewal! Complete payment to avoid coverage gaps.",
                type = "RENEWAL"
            )
            repository.insertReminder(reminder)
            
            // Mark policy update
            repository.updatePolicy(policy.copy(status = "RENEWAL_DUE"))
        }
    }

    fun triggerPaymentReminder(policy: Policy) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val reminder = Reminder(
                userEmail = user.email,
                title = "💳 Monthly Premium Reminder",
                message = "Premium autopay of ₹${String.format("%.2f", policy.premiumAmount)} for policy '${policy.policyNumber}' is scheduled in 3 days.",
                type = "PAYMENT"
            )
            repository.insertReminder(reminder)
        }
    }

    // ADMIN ACTIONS
    fun adminUpdateClaimStatus(claim: Claim, approve: Boolean, remarks: String) {
        viewModelScope.launch {
            val finalStatus = if (approve) "APPROVED" else "REJECTED"
            val updated = claim.copy(status = finalStatus, remarks = remarks)
            repository.updateClaim(updated)

            // Send Realtime Claim Reminder to User Account
            val titleText = if (approve) "Claim approved! ✅" else "Claim rejected ❌"
            val approvalMsg = if (approve) {
                "Great news! Your claim for ₹${String.format("%.2f", claim.claimAmount)} under Policy # ${claim.policyNumber} has been approved. Transferred to your registered account soon. Admin note: $remarks"
            } else {
                "We regret to inform you that your claim for ₹${String.format("%.2f", claim.claimAmount)} has been rejected. Details: $remarks"
            }

            val appReminder = Reminder(
                userEmail = claim.userEmail,
                title = titleText,
                message = approvalMsg,
                type = "CLAIM_UPDATE"
            )
            repository.insertReminder(appReminder)
        }
    }

    fun adminCreatePlan(
        title: String,
        type: String,
        description: String,
        basePremium: Double,
        coverageAmount: Double,
        features: String
    ) {
        viewModelScope.launch {
            val newPlan = InsurancePlan(
                title = title,
                type = type,
                description = description,
                basePremium = basePremium,
                coverageAmount = coverageAmount,
                features = features,
                isCustom = true
            )
            repository.insertPlan(newPlan)
        }
    }

    fun adminDeletePlan(plan: InsurancePlan) {
        viewModelScope.launch {
            repository.deletePlanById(plan.id)
        }
    }

    fun adminDeleteUser(email: String) {
        viewModelScope.launch {
            repository.deleteUser(email)
        }
    }

    // PREMIUM ESTIMATION RULES
    fun calculateEstimatedPremium(
        plan: InsurancePlan,
        age: Int,
        tobaccoUser: Boolean,
        preExistingConditions: Boolean,
        coverageLevel: String, // "Standard", "Value", "Premium"
        vehicleType: String = "Sedan", // "Sedan", "SUV", "Sports", "Bike", "Truck"
        drivingRecord: String = "Clean" // "Clean", "Minor Violations", "Major Offenses"
    ): Double {
        var premium = plan.basePremium

        // Age factor
        premium *= when {
            age < 25 -> 1.35  // Younger, higher risk (especially vehicle)
            age in 25..49 -> 1.0
            age in 50..65 -> 1.25
            else -> 1.55 // Higher age risk
        }

        // Plan types specific modification
        if (plan.type == "HEALTH") {
            if (tobaccoUser) premium *= 1.45
            if (preExistingConditions) premium *= 1.35
            premium *= when (coverageLevel) {
                "Value" -> 0.85
                "Premium" -> 1.3
                else -> 1.0
            }
        } else if (plan.type == "VEHICLE") {
            premium *= when (vehicleType) {
                "SUV" -> 1.15
                "Sports" -> 1.6
                "Bike" -> 0.7
                "Truck" -> 1.3
                else -> 1.0 // Sedan
            }
            premium *= when (drivingRecord) {
                "Minor Violations" -> 1.25
                "Major Offenses" -> 1.75
                else -> 1.0 // Clean
            }
        } else if (plan.type == "LIFE") {
            if (tobaccoUser) premium *= 1.8
            if (preExistingConditions) premium *= 1.5
            if (age > 50) premium *= 2.0
        }

        return premium
    }
}
