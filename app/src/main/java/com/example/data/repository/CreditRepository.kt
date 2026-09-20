package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.domain.model.CreditPackage
import com.example.domain.model.SubscriptionTier
import com.example.domain.model.UserCreditProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Calendar

class CreditRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("agent_credits_prefs", Context.MODE_PRIVATE)

    private val _creditProfile = MutableStateFlow(loadProfile())
    val creditProfile: StateFlow<UserCreditProfile> = _creditProfile.asStateFlow()

    init {
        checkDailyBonus()
    }

    val availableCreditPackages = listOf(
        CreditPackage(
            id = "pkg_starter",
            nameEn = "Starter AI Boost",
            nameBn = "স্টার্টার বুস্ট",
            credits = 100,
            priceUsd = "$1.99",
            priceBdt = "৳১৯৯",
            isPopular = false
        ),
        CreditPackage(
            id = "pkg_growth",
            nameEn = "Executive Growth Pack",
            nameBn = "এক্সিকিউটিভ প্যাক",
            credits = 500,
            priceUsd = "$4.99",
            priceBdt = "৳৪৯৯",
            isPopular = true
        ),
        CreditPackage(
            id = "pkg_power",
            nameEn = "Autonomous Powerhouse",
            nameBn = "মাস্টার পাওয়ার প্যাক",
            credits = 2000,
            priceUsd = "$14.99",
            priceBdt = "৳১,৪৯৯",
            isPopular = false
        )
    )

    private fun loadProfile(): UserCreditProfile {
        val credits = prefs.getInt("credits", 50)
        val tierName = prefs.getString("subscription_tier", SubscriptionTier.FREE.name) ?: SubscriptionTier.FREE.name
        val tier = try { SubscriptionTier.valueOf(tierName) } catch (e: Exception) { SubscriptionTier.FREE }
        val currency = prefs.getString("currency", "USD") ?: "USD"
        val tasks = prefs.getInt("total_tasks", 0)
        val strategies = prefs.getInt("total_strategies", 0)
        val lastBonus = prefs.getLong("last_bonus_date", 0L)

        return UserCreditProfile(
            credits = credits,
            tier = tier,
            currency = currency,
            totalTasksExecuted = tasks,
            totalStrategiesCompleted = strategies,
            lastBonusDate = lastBonus
        )
    }

    private fun saveProfile(profile: UserCreditProfile) {
        prefs.edit()
            .putInt("credits", profile.credits)
            .putString("subscription_tier", profile.tier.name)
            .putString("currency", profile.currency)
            .putInt("total_tasks", profile.totalTasksExecuted)
            .putInt("total_strategies", profile.totalStrategiesCompleted)
            .putLong("last_bonus_date", profile.lastBonusDate)
            .apply()
        _creditProfile.value = profile
    }

    fun hasCredits(required: Int = 1): Boolean {
        if (_creditProfile.value.tier == SubscriptionTier.ENTERPRISE_BOSS) return true
        return _creditProfile.value.credits >= required
    }

    fun deductCredits(amount: Int = 1): Boolean {
        val current = _creditProfile.value
        if (current.tier == SubscriptionTier.ENTERPRISE_BOSS) {
            saveProfile(current.copy(totalTasksExecuted = current.totalTasksExecuted + 1))
            return true
        }
        if (current.credits >= amount) {
            saveProfile(
                current.copy(
                    credits = current.credits - amount,
                    totalTasksExecuted = current.totalTasksExecuted + 1
                )
            )
            return true
        }
        return false
    }

    fun purchaseCredits(pkg: CreditPackage): Boolean {
        val current = _creditProfile.value
        val newCredits = current.credits + pkg.credits
        saveProfile(current.copy(credits = newCredits))
        return true
    }

    fun upgradeSubscription(tier: SubscriptionTier): Boolean {
        val current = _creditProfile.value
        val addedCredits = when (tier) {
            SubscriptionTier.FREE -> 0
            SubscriptionTier.PRO_MASTER -> 2500
            SubscriptionTier.ENTERPRISE_BOSS -> 10000
        }
        saveProfile(
            current.copy(
                tier = tier,
                credits = current.credits + addedCredits
            )
        )
        return true
    }

    fun toggleCurrency() {
        val current = _creditProfile.value
        val newCurrency = if (current.currency == "USD") "BDT" else "USD"
        saveProfile(current.copy(currency = newCurrency))
    }

    fun recordStrategyCompleted() {
        val current = _creditProfile.value
        saveProfile(current.copy(totalStrategiesCompleted = current.totalStrategiesCompleted + 1))
    }

    private fun checkDailyBonus() {
        val current = _creditProfile.value
        val now = System.currentTimeMillis()
        val calNow = Calendar.getInstance().apply { timeInMillis = now }
        val calLast = Calendar.getInstance().apply { timeInMillis = current.lastBonusDate }

        val isDifferentDay = calNow.get(Calendar.DAY_OF_YEAR) != calLast.get(Calendar.DAY_OF_YEAR) ||
                calNow.get(Calendar.YEAR) != calLast.get(Calendar.YEAR)

        if (isDifferentDay) {
            val bonus = when (current.tier) {
                SubscriptionTier.FREE -> 30
                SubscriptionTier.PRO_MASTER -> 100
                SubscriptionTier.ENTERPRISE_BOSS -> 500
            }
            saveProfile(
                current.copy(
                    credits = current.credits + bonus,
                    lastBonusDate = now
                )
            )
        }
    }
}
