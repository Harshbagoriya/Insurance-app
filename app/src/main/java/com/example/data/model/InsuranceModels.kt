package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserAccount(
    @PrimaryKey val email: String,
    val name: String,
    val role: String, // "USER" or "ADMIN"
    val profilePicUrl: String? = null,
    val joinedDate: Long = System.currentTimeMillis()
)

@Entity(tableName = "insurance_plans")
data class InsurancePlan(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val type: String, // "HEALTH", "VEHICLE", "LIFE", "HOME"
    val description: String,
    val basePremium: Double,
    val coverageAmount: Double,
    val features: String, // Comma-separated or bullet list
    val isCustom: Boolean = false
)

@Entity(tableName = "policies")
data class Policy(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val policyNumber: String,
    val userEmail: String,
    val planId: Int,
    val planTitle: String,
    val type: String, // "HEALTH", "VEHICLE", "LIFE", "HOME"
    val insuredAge: Int,
    val coverageNeeds: String, // JSON or descriptive string
    val premiumAmount: Double,
    val startDate: Long,
    val endDate: Long,
    val status: String // "ACTIVE", "EXPIRED", "RENEWAL_DUE", "CLAIM_FILED"
)

@Entity(tableName = "claims")
data class Claim(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val policyId: Int,
    val policyNumber: String,
    val planTitle: String,
    val userEmail: String,
    val claimAmount: Double,
    val reason: String,
    val status: String, // "PENDING", "APPROVED", "REJECTED"
    val filedDate: Long = System.currentTimeMillis(),
    val remarks: String? = null
)

@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userEmail: String,
    val title: String,
    val message: String,
    val type: String, // "RENEWAL", "CLAIM_UPDATE", "PAYMENT"
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
