package com.example.domain.model

data class DailyCompanyReport(
    val dateString: String,
    val totalRevenue: Double,
    val totalExpenses: Double,
    val netCashFlow: Double,
    val activeUsers: Int,
    val tasksCompleted: Int,
    val executiveBriefingEn: String,
    val executiveBriefingBn: String,
    val executiveBriefingBanglish: String,
    val kpis: List<BusinessKpi>
)

data class BusinessKpi(
    val label: String,
    val value: String,
    val change: String,
    val isPositive: Boolean
)

enum class FinancialType {
    INCOME,
    EXPENSE
}

data class FinancialTransaction(
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val type: FinancialType,
    val category: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class BusinessStrategy(
    val id: String,
    val titleEn: String,
    val titleBn: String,
    val titleBanglish: String,
    val category: String,
    val summaryEn: String,
    val summaryBn: String,
    val summaryBanglish: String,
    val actionSteps: List<String>,
    val estimatedImpact: String,
    val bossPromptEn: String = "Boss, should I execute this strategy?",
    val bossPromptBn: String = "বস, আমি কি এই কৌশলটি এক্সিকিউট করব?",
    val bossPromptBanglish: String = "Boss, ami ki ei strategy execute korbo?",
    val isExecuted: Boolean = false
)

data class MarketingWorkflow(
    val id: String,
    val channel: String,
    val titleEn: String,
    val titleBn: String,
    val titleBanglish: String,
    val hookEn: String,
    val hookBn: String,
    val hookBanglish: String,
    val stepDetails: List<String>,
    val isZeroBudget: Boolean = true,
    val executionSchedule: String = "Daily 10:00 AM"
)
