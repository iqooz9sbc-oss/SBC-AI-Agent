package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.repository.AutomationRepository
import com.example.data.repository.BusinessRepository
import com.example.data.repository.CreditRepository
import com.example.data.repository.SettingsRepository
import com.example.domain.model.AutomationRule
import com.example.domain.model.AutonomousTask
import com.example.domain.model.BusinessStrategy
import com.example.domain.model.DailyCompanyReport
import com.example.domain.model.FinancialTransaction
import com.example.domain.model.FinancialType
import com.example.domain.model.MarketingWorkflow
import com.example.domain.model.SelfHealingMetrics
import com.example.engine.SelfHealingTaskCoordinator
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AutomationSubTab(val titleEn: String, val titleBn: String) {
    BUSINESS("Business & Workflows", "ব্যবসায় ও ওয়ার্কফ্লো"),
    SELF_HEALING("Autonomous Tasks", "স্ব-নিরাময় টাস্ক"),
    DEVICE_RULES("Device Routines", "ডিভাইস রুটিন")
}

class AutomationViewModel(
    private val automationRepository: AutomationRepository,
    private val settingsRepository: SettingsRepository,
    private val businessRepository: BusinessRepository,
    private val selfHealingCoordinator: SelfHealingTaskCoordinator,
    private val creditRepository: CreditRepository
) : ViewModel() {

    val rules: StateFlow<List<AutomationRule>> = automationRepository.getRules()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val settings = settingsRepository.settings

    val currentTask: StateFlow<AutonomousTask?> = selfHealingCoordinator.currentTask

    val selfHealingMetrics: StateFlow<SelfHealingMetrics> = selfHealingCoordinator.metrics

    val transactions: StateFlow<List<FinancialTransaction>> = businessRepository.transactions

    val strategies: StateFlow<List<BusinessStrategy>> = businessRepository.strategies

    val marketingWorkflows: StateFlow<List<MarketingWorkflow>> = businessRepository.marketingWorkflows

    private val _selectedSubTab = MutableStateFlow(AutomationSubTab.BUSINESS)
    val selectedSubTab: StateFlow<AutomationSubTab> = _selectedSubTab.asStateFlow()

    private val _dailyReport = MutableStateFlow(businessRepository.getDailyReport())
    val dailyReport: StateFlow<DailyCompanyReport> = _dailyReport.asStateFlow()

    private val _executionMessage = MutableStateFlow<String?>(null)
    val executionMessage: StateFlow<String?> = _executionMessage.asStateFlow()

    fun selectSubTab(tab: AutomationSubTab) {
        _selectedSubTab.value = tab
    }

    fun refreshDailyReport() {
        _dailyReport.value = businessRepository.getDailyReport()
        val isBn = settings.value.selectedLanguage.code == "bn"
        _executionMessage.value = if (isBn) "📊 দৈনিক কোম্পানি রিপোর্ট হালনাগাদ করা হয়েছে" else "📊 Daily Company Report updated with fresh metrics"
    }

    fun addFinancialTransaction(title: String, amount: Double, type: FinancialType, category: String) {
        businessRepository.addTransaction(title, amount, type, category)
        _dailyReport.value = businessRepository.getDailyReport()
        val isBn = settings.value.selectedLanguage.code == "bn"
        _executionMessage.value = if (isBn) "💰 লেনদেন সফলভাবে যুক্ত হয়েছে" else "💰 Transaction recorded to company ledger"
    }

    fun executeStrategy(strategy: BusinessStrategy) {
        if (!creditRepository.hasCredits(3)) {
            _executionMessage.value = "⚠️ Insufficient AI Credits. Please top-up in Settings."
            return
        }

        creditRepository.deductCredits(3)
        creditRepository.recordStrategyCompleted()
        businessRepository.markStrategyExecuted(strategy.id)

        selfHealingCoordinator.startAutonomousTask(
            titleEn = strategy.titleEn,
            titleBn = strategy.titleBn,
            titleBanglish = strategy.titleBanglish,
            description = strategy.summaryEn,
            simulateSelfHealing = true
        )

        _selectedSubTab.value = AutomationSubTab.SELF_HEALING
        _executionMessage.value = "🚀 Executing Strategy: \"${strategy.titleEn}\""
    }

    fun launchMarketingWorkflow(workflow: MarketingWorkflow) {
        if (!creditRepository.hasCredits(2)) {
            _executionMessage.value = "⚠️ Insufficient AI Credits. Please top-up in Settings."
            return
        }

        creditRepository.deductCredits(2)
        creditRepository.recordStrategyCompleted()

        selfHealingCoordinator.startAutonomousTask(
            titleEn = "Campaign: ${workflow.titleEn} (${workflow.channel})",
            titleBn = "ক্যাম্পেইন: ${workflow.titleBn}",
            titleBanglish = "Campaign: ${workflow.titleBanglish}",
            description = "Distributing organic zero-budget marketing assets across ${workflow.channel}.",
            simulateSelfHealing = false
        )

        _selectedSubTab.value = AutomationSubTab.SELF_HEALING
        _executionMessage.value = "📢 Marketing Workflow Launched: ${workflow.channel}"
    }

    fun startCustomAutonomousTask(title: String, simulateHealing: Boolean = true) {
        if (!creditRepository.hasCredits(2)) {
            _executionMessage.value = "⚠️ Insufficient AI Credits. Please top-up in Settings."
            return
        }

        creditRepository.deductCredits(2)

        selfHealingCoordinator.startAutonomousTask(
            titleEn = title,
            titleBn = title,
            titleBanglish = title,
            description = "Autonomous task execution pipeline with active self-healing monitors.",
            simulateSelfHealing = simulateHealing
        )
    }

    fun confirmSelfHealingFix() {
        selfHealingCoordinator.confirmSelfHealingFix()
        val isBn = settings.value.selectedLanguage.code == "bn"
        _executionMessage.value = if (isBn) "🛠️ বস অনুমোদন দিয়েছেন! সমাধান প্রয়োগ করে টাস্ক পুনরায় চলছে।" else "🛠️ Fix applied! Self-healing engine resumed execution."
    }

    fun cancelAutonomousTask() {
        selfHealingCoordinator.cancelTask()
    }

    fun dismissAutonomousTask() {
        selfHealingCoordinator.dismissTask()
    }

    fun toggleRule(rule: AutomationRule) {
        viewModelScope.launch {
            automationRepository.setRuleEnabled(rule.id, !rule.isEnabled)
        }
    }

    fun executeRule(rule: AutomationRule) {
        viewModelScope.launch {
            automationRepository.markExecuted(rule.id)
            val isBn = settings.value.selectedLanguage.code == "bn"
            val message = if (isBn) {
                "⚡ রুটিন সফলভাবে চালু হয়েছে: \"${rule.titleBn}\"\nঅ্যাকশন: ${rule.actionTextBn}"
            } else {
                "⚡ Routine Triggered: \"${rule.title}\"\nAction: ${rule.actionText}"
            }
            _executionMessage.value = message
            delay(4000)
            if (_executionMessage.value == message) {
                _executionMessage.value = null
            }
        }
    }

    fun addRule(
        title: String,
        titleBn: String,
        description: String,
        descriptionBn: String,
        trigger: String,
        action: String
    ) {
        if (title.isBlank() || action.isBlank()) return
        viewModelScope.launch {
            automationRepository.saveRule(
                AutomationRule(
                    title = title.trim(),
                    titleBn = titleBn.ifBlank { title.trim() },
                    description = description.trim(),
                    descriptionBn = descriptionBn.ifBlank { description.trim() },
                    triggerText = trigger.trim(),
                    triggerTextBn = trigger.trim(),
                    actionText = action.trim(),
                    actionTextBn = action.trim(),
                    isEnabled = true
                )
            )
        }
    }

    fun deleteRule(id: Long) {
        viewModelScope.launch {
            automationRepository.deleteRule(id)
        }
    }

    fun clearExecutionMessage() {
        _executionMessage.value = null
    }

    class Factory(
        private val automationRepository: AutomationRepository,
        private val settingsRepository: SettingsRepository,
        private val businessRepository: BusinessRepository,
        private val selfHealingCoordinator: SelfHealingTaskCoordinator,
        private val creditRepository: CreditRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AutomationViewModel(
                automationRepository,
                settingsRepository,
                businessRepository,
                selfHealingCoordinator,
                creditRepository
            ) as T
        }
    }
}
