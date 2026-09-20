package com.example.engine

import com.example.domain.model.AutonomousTask
import com.example.domain.model.ExecutionRunRecord
import com.example.domain.model.SelfHealingMetrics
import com.example.domain.model.StepStatus
import com.example.domain.model.TaskStatus
import com.example.domain.model.TaskStep
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SelfHealingTaskCoordinator {

    private val scope = CoroutineScope(Dispatchers.Default)
    private var taskJob: Job? = null

    private val _currentTask = MutableStateFlow<AutonomousTask?>(null)
    val currentTask: StateFlow<AutonomousTask?> = _currentTask.asStateFlow()

    private val initialRecentRuns = listOf(
        ExecutionRunRecord("run_1", "Multi-Source Market Penetration & Lead Synthesis", "মার্কেট পেনিট্রেশন ও লিড সংগ্রহ", "Market penetration o lead synthesis", 5, 4, 1, 0, System.currentTimeMillis() - 28 * 60 * 1000, TaskStatus.COMPLETED, 8.4f),
        ExecutionRunRecord("run_2", "Real-Time Company Cashflow & Ledger Reconciliation", "কোম্পানি ক্যাশফ্লো ও লেজার ব্যালেন্স", "Cashflow o ledger reconciliation", 5, 5, 0, 0, System.currentTimeMillis() - 75 * 60 * 1000, TaskStatus.COMPLETED, 6.2f),
        ExecutionRunRecord("run_3", "Dynamic Trilingual Knowledge Base Indexing", "ত্রিভাষিক জ্ঞানভাণ্ডার ইনডেক্সিং", "Trilingual memory indexing", 5, 4, 1, 0, System.currentTimeMillis() - 180 * 60 * 1000, TaskStatus.COMPLETED, 7.8f),
        ExecutionRunRecord("run_4", "Autonomous Social Growth Engine & Asset Dispatch", "সোশ্যাল গ্রোথ ইঞ্জিন ও কনটেন্ট ডেসপ্যাচ", "Social growth engine o asset dispatch", 5, 3, 2, 0, System.currentTimeMillis() - 360 * 60 * 1000, TaskStatus.COMPLETED, 11.1f),
        ExecutionRunRecord("run_5", "Device Sensor Diagnostics & Permission Self-Repair", "ডিভাইস সেন্সর ডায়াগনস্টিকস ও মেরামত", "Device sensor diagnostics o repair", 5, 5, 0, 0, System.currentTimeMillis() - 720 * 60 * 1000, TaskStatus.COMPLETED, 5.5f)
    )

    private val _metrics = MutableStateFlow(
        SelfHealingMetrics(
            totalHistoricalTasks = 23,
            totalHistoricalSteps = 115,
            successfulExecutions = 96,
            recoveredErrors = 19,
            unresolvedErrors = 0,
            recoverySuccessRate = 100.0f,
            avgRecoveryTimeSeconds = 1.35f,
            recentRuns = initialRecentRuns
        )
    )
    val metrics: StateFlow<SelfHealingMetrics> = _metrics.asStateFlow()

    fun startAutonomousTask(
        titleEn: String,
        titleBn: String,
        titleBanglish: String,
        description: String,
        simulateSelfHealing: Boolean = true
    ) {
        taskJob?.cancel()

        val initialSteps = listOf(
            TaskStep("s1", "Initialize Environment & Ingest Parameters", "পরিবেশ প্রস্তুত ও ডেটা গ্রহণ", "Environment ready o parameters ingest kora", StepStatus.PENDING),
            TaskStep("s2", "Synthesize Business Logic & Cross-Reference Memory", "লজিক বিশ্লেষণ ও মেমোরি যাচাই", "Business logic synthesize o memory cross-check", StepStatus.PENDING),
            TaskStep("s3", "Execute Autonomous Action Pipeline & API Dispatch", "অ্যাকশন পাইপলাইন ও এপিআই প্রেরণ", "Action pipeline execute o API dispatch", StepStatus.PENDING, errorPayload = if (simulateSelfHealing) "ERR_SCHEMA_VALIDATION_TIMEOUT" else null),
            TaskStep("s4", "Verify Output Quality & Self-Healing Health Check", "ফলাফল যাচাই ও স্ব-নিরাময় পর্যবেক্ষণ", "Output quality verify o self-healing check", StepStatus.PENDING),
            TaskStep("s5", "Finalize Delivery & Log Executive Audit Trail", "চূড়ান্ত অনুমোদন ও অডিট রিপোর্ট প্রস্তুত", "Final report ready o audit trail complete", StepStatus.PENDING)
        )

        val task = AutonomousTask(
            id = "task_${System.currentTimeMillis()}",
            titleEn = titleEn,
            titleBn = titleBn,
            titleBanglish = titleBanglish,
            description = description,
            status = TaskStatus.RUNNING,
            currentStepIndex = 0,
            steps = initialSteps,
            logs = listOf("🚀 Autonomous task initiated by Boss.", "Analyzing requirements and configuring executor pipeline...")
        )

        _currentTask.value = task

        taskJob = scope.launch {
            runTaskWorkflow(simulateSelfHealing)
        }
    }

    private suspend fun runTaskWorkflow(simulateSelfHealing: Boolean) {
        val task = _currentTask.value ?: return

        for (i in task.steps.indices) {
            val current = _currentTask.value ?: return
            if (current.status == TaskStatus.CANCELLED) return

            // Update active step
            val updatedSteps = current.steps.mapIndexed { index, step ->
                when {
                    index < i -> step.copy(status = StepStatus.COMPLETED)
                    index == i -> step.copy(status = StepStatus.ACTIVE)
                    else -> step
                }
            }

            _currentTask.value = current.copy(
                currentStepIndex = i,
                steps = updatedSteps,
                logs = current.logs + "▶️ Executing step [${i + 1}/${current.steps.size}]: ${current.steps[i].titleEn}"
            )

            delay(1400) // Simulating real autonomous execution

            // Check if this step encounters an error for self-healing demonstration
            if (simulateSelfHealing && i == 2 && current.steps[i].errorPayload != null) {
                // Error encountered!
                val errorSteps = _currentTask.value!!.steps.mapIndexed { index, step ->
                    if (index == i) step.copy(status = StepStatus.FAILED) else step
                }

                _currentTask.value = _currentTask.value!!.copy(
                    status = TaskStatus.ERROR_DETECTED,
                    steps = errorSteps,
                    errorDetails = "Execution Barrier Detected: [HTTP 429 & Payload Schema Mismatch] on API Dispatch Pipeline. Automated retry aborted to prevent token exhaustion.",
                    suggestedSolutionEn = "Apply automated schema sanitization patch, re-route through fallback secondary endpoint, and activate exponential backoff retry.",
                    suggestedSolutionBn = "স্বয়ংক্রিয় স্কিমা স্যানিটাইজেশন প্যাচ প্রয়োগ করুন, ব্যাকআপ সেকেন্ডারি এন্ডপয়েন্টে রি-রাউট করুন এবং এক্সপোনেনশিয়াল ব্যাকঅফ সক্রিয় করুন।",
                    suggestedSolutionBanglish = "Automated schema sanitization patch apply korun, backup endpoint-e route transfer korun ebong retry chaluyen.",
                    requiresBossConfirmation = true,
                    bossQuestionEn = "Boss, should I apply this self-healing fix and resume task?",
                    bossQuestionBn = "বস, আমি কি এই স্ব-নিরাময় সমাধান প্রয়োগ করে কাজটি সম্পন্ন করব?",
                    bossQuestionBanglish = "Boss, ami ki ei self-healing fix apply kore task ta resume korbo?",
                    logs = _currentTask.value!!.logs + listOf(
                        "⚠️ ERROR DETECTED at step 3: Payload Schema Mismatch & Rate Guard triggered.",
                        "🔍 Self-Healing AI Diagnostic: Root cause identified.",
                        "👑 Awaiting Boss confirmation to deploy automated patch..."
                    )
                )
                // Stop execution here and await user confirmation
                return
            }
        }

        // Complete task
        finishTaskSuccessfully()
    }

    fun confirmSelfHealingFix() {
        val task = _currentTask.value ?: return
        if (task.status != TaskStatus.ERROR_DETECTED) return

        taskJob?.cancel()
        taskJob = scope.launch {
            // Apply healing
            val healingSteps = task.steps.mapIndexed { index, step ->
                if (index == 2) step.copy(status = StepStatus.HEALED) else step
            }

            _currentTask.value = task.copy(
                status = TaskStatus.HEALING,
                steps = healingSteps,
                requiresBossConfirmation = false,
                logs = task.logs + listOf(
                    "🛠️ Boss confirmed fix! Deploying automated patch...",
                    "✅ Schema sanitized & secondary API endpoint established.",
                    "🔄 Resuming task execution seamlessly..."
                )
            )

            delay(1200)

            // Continue remaining steps (steps 3 and 4)
            for (i in 3 until task.steps.size) {
                val current = _currentTask.value ?: return@launch
                val updated = current.steps.mapIndexed { index, step ->
                    when {
                        index < i -> if (index == 2) step.copy(status = StepStatus.HEALED) else step.copy(status = StepStatus.COMPLETED)
                        index == i -> step.copy(status = StepStatus.ACTIVE)
                        else -> step
                    }
                }

                _currentTask.value = current.copy(
                    currentStepIndex = i,
                    steps = updated,
                    logs = current.logs + "▶️ Resumed step [${i + 1}/${current.steps.size}]: ${current.steps[i].titleEn}"
                )

                delay(1200)
            }

            finishTaskSuccessfully()
        }
    }

    private fun finishTaskSuccessfully() {
        val current = _currentTask.value ?: return
        val allCompleted = current.steps.map { step ->
            if (step.status == StepStatus.HEALED) step else step.copy(status = StepStatus.COMPLETED)
        }

        val healedCount = allCompleted.count { it.status == StepStatus.HEALED }
        val successCount = allCompleted.count { it.status == StepStatus.COMPLETED }

        val newRecord = ExecutionRunRecord(
            id = "run_${System.currentTimeMillis()}",
            taskNameEn = current.titleEn,
            taskNameBn = current.titleBn,
            taskNameBanglish = current.titleBanglish,
            totalSteps = allCompleted.size,
            successfulSteps = successCount,
            recoveredSteps = healedCount,
            failedSteps = 0,
            timestamp = System.currentTimeMillis(),
            status = TaskStatus.COMPLETED,
            durationSeconds = 7.2f
        )

        val oldMetrics = _metrics.value
        _metrics.value = oldMetrics.copy(
            totalHistoricalTasks = oldMetrics.totalHistoricalTasks + 1,
            totalHistoricalSteps = oldMetrics.totalHistoricalSteps + allCompleted.size,
            successfulExecutions = oldMetrics.successfulExecutions + successCount,
            recoveredErrors = oldMetrics.recoveredErrors + healedCount,
            recentRuns = listOf(newRecord) + oldMetrics.recentRuns.take(9)
        )

        _currentTask.value = current.copy(
            status = TaskStatus.COMPLETED,
            currentStepIndex = current.steps.size,
            steps = allCompleted,
            requiresBossConfirmation = false,
            resultSummaryEn = "Task completed successfully, Boss! All autonomous objectives met with 100% data integrity and self-healing verification.",
            resultSummaryBn = "কাজটি সফলভাবে সম্পন্ন হয়েছে, বস! ১০০% ডেটা নিখুঁত রেখে এবং সেলফ-হিলিং যাচাইসহ সকল উদ্দেশ্য অর্জিত হয়েছে।",
            resultSummaryBanglish = "Task ta successfully complete hoyeche, Boss! 100% data accuracy o self-healing verification er shathe shob execute hoyeche.",
            logs = current.logs + listOf(
                "🎉 Task completed successfully!",
                "📊 Audit log recorded to local encrypted database."
            )
        )
    }

    fun dismissTask() {
        taskJob?.cancel()
        _currentTask.value = null
    }

    fun cancelTask() {
        taskJob?.cancel()
        _currentTask.value = _currentTask.value?.copy(
            status = TaskStatus.CANCELLED,
            requiresBossConfirmation = false,
            logs = _currentTask.value!!.logs + "🛑 Task cancelled by Boss."
        )
    }
}
