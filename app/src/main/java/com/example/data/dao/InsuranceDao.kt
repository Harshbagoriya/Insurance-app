package com.example.data.dao

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface InsuranceDao {

    // Users
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserAccount?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserAccount)

    @Query("DELETE FROM users WHERE email = :email")
    suspend fun deleteUserByEmail(email: String)

    @Query("SELECT * FROM users")
    fun getAllUsersFlow(): Flow<List<UserAccount>>

    // Insurance Plans
    @Query("SELECT * FROM insurance_plans")
    fun getAllPlansFlow(): Flow<List<InsurancePlan>>

    @Query("SELECT * FROM insurance_plans")
    suspend fun getAllPlans(): List<InsurancePlan>

    @Query("SELECT * FROM insurance_plans WHERE id = :id LIMIT 1")
    suspend fun getPlanById(id: Int): InsurancePlan?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlan(plan: InsurancePlan)

    @Query("DELETE FROM insurance_plans WHERE id = :id")
    suspend fun deletePlanById(id: Int)

    // Policies
    @Query("SELECT * FROM policies WHERE userEmail = :email ORDER BY startDate DESC")
    fun getPoliciesByUserFlow(email: String): Flow<List<Policy>>

    @Query("SELECT * FROM policies ORDER BY startDate DESC")
    fun getAllPoliciesFlow(): Flow<List<Policy>>

    @Query("SELECT * FROM policies WHERE id = :id LIMIT 1")
    suspend fun getPolicyById(id: Int): Policy?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPolicy(policy: Policy)

    @Update
    suspend fun updatePolicy(policy: Policy)

    @Query("DELETE FROM policies WHERE userEmail = :email")
    suspend fun deletePoliciesByUserEmail(email: String)

    // Claims
    @Query("SELECT * FROM claims WHERE userEmail = :email ORDER BY filedDate DESC")
    fun getClaimsByUserFlow(email: String): Flow<List<Claim>>

    @Query("SELECT * FROM claims ORDER BY filedDate DESC")
    fun getAllClaimsFlow(): Flow<List<Claim>>

    @Query("SELECT * FROM claims WHERE id = :id LIMIT 1")
    suspend fun getClaimById(id: Int): Claim?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClaim(claim: Claim)

    @Update
    suspend fun updateClaim(claim: Claim)

    @Query("DELETE FROM claims WHERE userEmail = :email")
    suspend fun deleteClaimsByUserEmail(email: String)

    // Reminders
    @Query("SELECT * FROM reminders WHERE userEmail = :email ORDER BY timestamp DESC")
    fun getRemindersByUserFlow(email: String): Flow<List<Reminder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: Reminder)

    @Update
    suspend fun updateReminder(reminder: Reminder)

    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun deleteReminderById(id: Int)

    @Query("DELETE FROM reminders WHERE userEmail = :email")
    suspend fun clearAllRemindersForUser(email: String)
}
