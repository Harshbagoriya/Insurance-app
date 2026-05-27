package com.example.data.repository

import com.example.data.dao.InsuranceDao
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

class InsuranceRepository(private val dao: InsuranceDao) {

    // User Operations
    suspend fun getUserByEmail(email: String): UserAccount? = dao.getUserByEmail(email)
    suspend fun insertUser(user: UserAccount) = dao.insertUser(user)
    fun getAllUsersFlow(): Flow<List<UserAccount>> = dao.getAllUsersFlow()
    suspend fun deleteUser(email: String) {
        dao.deletePoliciesByUserEmail(email)
        dao.deleteClaimsByUserEmail(email)
        dao.clearAllRemindersForUser(email)
        dao.deleteUserByEmail(email)
    }

    // Insurance Plan Operations
    fun getAllPlansFlow(): Flow<List<InsurancePlan>> = dao.getAllPlansFlow()
    suspend fun getPlanById(id: Int): InsurancePlan? = dao.getPlanById(id)
    suspend fun insertPlan(plan: InsurancePlan) = dao.insertPlan(plan)
    suspend fun deletePlanById(id: Int) = dao.deletePlanById(id)

    // Policy Operations
    fun getPoliciesByUserFlow(email: String): Flow<List<Policy>> = dao.getPoliciesByUserFlow(email)
    fun getAllPoliciesFlow(): Flow<List<Policy>> = dao.getAllPoliciesFlow()
    suspend fun getPolicyById(id: Int) = dao.getPolicyById(id)
    suspend fun insertPolicy(policy: Policy) = dao.insertPolicy(policy)
    suspend fun updatePolicy(policy: Policy) = dao.updatePolicy(policy)

    // Claim Operations
    fun getClaimsByUserFlow(email: String): Flow<List<Claim>> = dao.getClaimsByUserFlow(email)
    fun getAllClaimsFlow(): Flow<List<Claim>> = dao.getAllClaimsFlow()
    suspend fun getClaimById(id: Int) = dao.getClaimById(id)
    suspend fun insertClaim(claim: Claim) = dao.insertClaim(claim)
    suspend fun updateClaim(claim: Claim) = dao.updateClaim(claim)

    // Reminder Operations
    fun getRemindersByUserFlow(email: String): Flow<List<Reminder>> = dao.getRemindersByUserFlow(email)
    suspend fun insertReminder(reminder: Reminder) = dao.insertReminder(reminder)
    suspend fun updateReminder(reminder: Reminder) = dao.updateReminder(reminder)
    suspend fun deleteReminderById(id: Int) = dao.deleteReminderById(id)
    suspend fun clearAllRemindersForUser(email: String) = dao.clearAllRemindersForUser(email)

    // Database Seeding Logic
    suspend fun seedDatabaseIfEmpty() {
        val existingPlans = dao.getAllPlans()
        if (existingPlans.isEmpty()) {
            val defaultPlans = listOf(
                InsurancePlan(
                    title = "Titanium Health Premium",
                    type = "HEALTH",
                    description = "No-compromise, premium healthcare protection for you and your family.",
                    basePremium = 799.0,
                    coverageAmount = 1000000.0,
                    features = "Full pre-existing coverage, Global in-patient care, Unlimited dental & optical benefits, Mental health services, No copay required"
                ),
                InsurancePlan(
                    title = "Classic Family Shield",
                    type = "HEALTH",
                    description = "Essential, highly affordable healthcare for growing families.",
                    basePremium = 399.0,
                    coverageAmount = 500000.0,
                    features = "In-panel hospital cashless billing, Free annual full-body checkup, Co-pay rate capped at 10%, Accident emergencies 100% covered"
                ),
                InsurancePlan(
                    title = "DriveSecure Comprehensive",
                    type = "VEHICLE",
                    description = "Complete peace of mind on the road with total vehicle protection for cars.",
                    basePremium = 499.0,
                    coverageAmount = 750000.0,
                    features = "Zero Depreciation on claim parts, Multi-country roadside breakdown towing, Engine and gearbox water-lock cover, Third-party passenger injury liability"
                ),
                InsurancePlan(
                    title = "Two-Wheeler Super Cover",
                    type = "VEHICLE",
                    description = "Affordable legal and comprehensive coverage for bikes and scooters.",
                    basePremium = 149.0,
                    coverageAmount = 150000.0,
                    features = "Regulatory third-party property damage coverage, Quick digital filing compensation, 24/7 tele-assistance helpline"
                ),
                InsurancePlan(
                    title = "SafeHaven Term Life Plan",
                    type = "LIFE",
                    description = "Secure your family's financial future with guaranteed assurance.",
                    basePremium = 299.0,
                    coverageAmount = 5000000.0,
                    features = "Lump-sum direct payout to nominees, Critical terminal illness advance benefit, Premium waivers on disability, Flexible claims dispatch"
                ),
                InsurancePlan(
                    title = "SmartHome Shield",
                    type = "HOME",
                    description = "Protect your home structure, appliances, and valuables in style.",
                    basePremium = 199.0,
                    coverageAmount = 250000.0,
                    features = "Fire, theft & vandalism protection, Natural disaster coverage, Valuables & electronic appliances replacement, Temporary accommodation compensation"
                )
            )

            for (plan in defaultPlans) {
                dao.insertPlan(plan)
            }
        }
    }
}
