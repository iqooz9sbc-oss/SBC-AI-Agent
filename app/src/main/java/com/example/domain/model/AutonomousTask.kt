package com.example.domain.model

enum class TaskStatus {
    IDLE,
    RUNNING,
    ERROR_DETECTED,
    HEALING,
    COMPLETED,
    CANCELLED
}

enum class StepStatus {
    PENDING,
    ACTIVE,
    COMPLETED,
    FAILED,
    HEALED
}

data class TaskStep(
    val id: String,
    val titleEn: String,
    val titleBn: String,
    val titleBanglish: String,
    var status: StepStatus = StepStatus.PENDING,
    val errorPayload: String? = null
)

data class AutonomousTask(
    val id: String,
    val titleEn: String,
    val titleBn: String,
    val titleBanglish: String,
    val description: String,
    val status: TaskStatus = TaskStatus.IDLE,
    val currentStepIndex: Int = 0,
    val steps: List<TaskStep>,
    val errorDetails: String? = null,
    val suggestedSolutionEn: String? = null,
    val suggestedSolutionBn: String? = null,
    val suggestedSolutionBanglish: String? = null,
    val requiresBossConfirmation: Boolean = false,
    val bossQuestionEn: String = "Boss, should I apply this self-healing fix and resume task?",
    val bossQuestionBn: String = "বস, আমি কি এই স্ব-নিরাময় সমাধান প্রয়োগ করে কাজটি সম্পন্ন করব?",
    val bossQuestionBanglish: String = "Boss, ami ki ei self-healing fix apply kore task ta resume korbo?",
    val logs: List<String> = emptyList(),
    val resultSummaryEn: String? = null,
    val resultSummaryBn: String? = null,
    val resultSummaryBanglish: String? = null
)

data class ExecutionRunRecord(
    val id: String,
    val taskNameEn: String,
    val taskNameBn: String,
    val taskNameBanglish: String,
    val totalSteps: Int,
    val successfulSteps: Int,
    val recoveredSteps: Int,
    val failedSteps: Int,
    val timestamp: Long,
    val status: TaskStatus,
    val durationSeconds: Float
)

data class SelfHealingMetrics(
    val totalHistoricalTasks: Int = 18,
    val totalHistoricalSteps: Int = 90,
    val successfulExecutions: Int = 74,
    val recoveredErrors: Int = 16,
    val unresolvedErrors: Int = 0,
    val recoverySuccessRate: Float = 100.0f,
    val avgRecoveryTimeSeconds: Float = 1.35f,
    val recentRuns: List<ExecutionRunRecord> = emptyList()
)
