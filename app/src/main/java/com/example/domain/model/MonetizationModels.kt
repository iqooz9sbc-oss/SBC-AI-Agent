package com.example.domain.model

enum class SubscriptionTier(
    val displayNameEn: String,
    val displayNameBn: String,
    val priceUsd: String,
    val priceBdt: String,
    val monthlyCredits: Int,
    val badge: String
) {
    FREE(
        "Free Starter",
        "ফ্রি স্টার্টার",
        "$0/mo",
        "৳০/মাস",
        50,
        "Free"
    ),
    PRO_MASTER(
        "Pro Master Agent",
        "প্রো মাস্টার এজেন্ট",
        "$9.99/mo",
        "৳৯৯৯/মাস",
        2500,
        "PRO"
    ),
    ENTERPRISE_BOSS(
        "Enterprise Boss",
        "এন্টারপ্রাইজ বস",
        "$29.99/mo",
        "৳২,৯৯০/মাস",
        10000,
        "BOSS"
    )
}

data class CreditPackage(
    val id: String,
    val nameEn: String,
    val nameBn: String,
    val credits: Int,
    val priceUsd: String,
    val priceBdt: String,
    val isPopular: Boolean = false
)

data class UserCreditProfile(
    val credits: Int = 50,
    val tier: SubscriptionTier = SubscriptionTier.FREE,
    val currency: String = "USD", // "USD" or "BDT"
    val totalTasksExecuted: Int = 0,
    val totalStrategiesCompleted: Int = 0,
    val lastBonusDate: Long = System.currentTimeMillis()
) {
    val useBdt: Boolean
        get() = currency == "BDT"
}
