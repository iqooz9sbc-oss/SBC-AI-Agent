package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.repository.BusinessRepository
import com.example.data.repository.ChatRepository
import com.example.data.repository.CreditRepository
import com.example.data.repository.MemoryRepository
import com.example.data.repository.SettingsRepository
import com.example.domain.model.AutonomousTask
import com.example.domain.model.ChatMessage
import com.example.domain.model.MessageSender
import com.example.domain.model.UserCreditProfile
import com.example.engine.AIAgentEngine
import com.example.engine.SelfHealingTaskCoordinator
import com.example.voice.VoiceController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class QuickPrompt(
    val titleEn: String,
    val titleBn: String,
    val titleBanglish: String,
    val queryEn: String,
    val queryBn: String,
    val queryBanglish: String
)

class ChatViewModel(
    private val chatRepository: ChatRepository,
    private val memoryRepository: MemoryRepository,
    private val settingsRepository: SettingsRepository,
    private val aiEngine: AIAgentEngine,
    private val voiceController: VoiceController,
    private val creditRepository: CreditRepository,
    private val selfHealingCoordinator: SelfHealingTaskCoordinator,
    private val businessRepository: BusinessRepository
) : ViewModel() {

    val messages: StateFlow<List<ChatMessage>> = chatRepository.getMessages()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val settings = settingsRepository.settings

    val creditProfile: StateFlow<UserCreditProfile> = creditRepository.creditProfile

    val currentAutonomousTask: StateFlow<AutonomousTask?> = selfHealingCoordinator.currentTask

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _isThinking = MutableStateFlow(false)
    val isThinking: StateFlow<Boolean> = _isThinking.asStateFlow()

    private val _attachedImageUri = MutableStateFlow<String?>(null)
    val attachedImageUri: StateFlow<String?> = _attachedImageUri.asStateFlow()

    private val _showCreditDialog = MutableStateFlow(false)
    val showCreditDialog: StateFlow<Boolean> = _showCreditDialog.asStateFlow()

    private val _showSelfHealingModal = MutableStateFlow(false)
    val showSelfHealingModal: StateFlow<Boolean> = _showSelfHealingModal.asStateFlow()

    val quickPrompts = listOf(
        QuickPrompt(
            "Executive Report",
            "কোম্পানি রিপোর্ট",
            "Company Report",
            "Give me today's daily executive company report and strategic recommendations, Boss.",
            "বস, আজকের কোম্পানি রিপোর্ট এবং কৌশলগত পরামর্শ দিন।",
            "Boss, ajker daily company report o strategic plan din."
        ),
        QuickPrompt(
            "Execute Strategy",
            "কৌশল বাস্তবায়ন",
            "Execute Strategy",
            "Recommend a zero-budget viral marketing strategy and ask me for confirmation.",
            "একটি জিরো-বাজেট ভাইরাল মার্কেটিং কৌশল প্রস্তাব করুন এবং বাস্তবায়নের অনুমতি চান।",
            "Zero-budget viral marketing strategy propose korun ebong Boss confirmation chan."
        ),
        QuickPrompt(
            "Test Self-Healing",
            "সেলফ-হিলিং টেস্ট",
            "Test Self-Healing",
            "Run an autonomous self-healing multi-step workflow to verify system recovery.",
            "সিস্টেমের স্ব-নিরাময় ক্ষমতা যাচাইয়ের জন্য একটি অটোনোমাস মাল্টি-স্টেপ টাস্ক চালান।",
            "Autonomous self-healing multi-step workflow run korun system test korte."
        ),
        QuickPrompt(
            "Accounting Summary",
            "হিসাব নিকাশ",
            "Accounting Summary",
            "Summarize our current revenues, operating expenses, and net margins.",
            "আমাদের মোট আয়, পরিচালন ব্যয় ও নিট মুনাফা মার্জিনের সারসংক্ষেপ দিন।",
            "Amader revenue, expense o profit margin-er summary din."
        ),
        QuickPrompt(
            "My Memories",
            "আমার মেমোরি কি?",
            "Amar Memories",
            "What do you remember about me from saved memories?",
            "আমার মেমোরিতে কি কি তথ্য সংরক্ষিত আছে?",
            "Amar saved memory-te ki ki info ache?"
        )
    )

    fun onInputChanged(newText: String) {
        _inputText.value = newText
    }

    fun attachImage(uri: String?) {
        _attachedImageUri.value = uri
    }

    fun openCreditStore(show: Boolean = true) {
        _showCreditDialog.value = show
    }

    fun openSelfHealingModal(show: Boolean = true) {
        _showSelfHealingModal.value = show
    }

    fun sendMessage(customPrompt: String? = null) {
        val query = (customPrompt ?: _inputText.value).trim()
        if (query.isBlank() && _attachedImageUri.value == null) return

        // Credit check
        if (!creditRepository.hasCredits(1)) {
            _showCreditDialog.value = true
            return
        }

        val currentSettings = settings.value
        val isBn = aiEngine.isBangla(query)
        val isBanglish = aiEngine.isBanglish(query)
        val imageToAttach = _attachedImageUri.value

        _inputText.value = ""
        _attachedImageUri.value = null

        viewModelScope.launch {
            creditRepository.deductCredits(1)

            val langCode = if (isBn) "bn" else if (isBanglish) "banglish" else "en"
            val userMsg = ChatMessage(
                sender = MessageSender.USER,
                content = query.ifBlank { "Sent an image for analysis." },
                isOnline = currentSettings.isOnlineMode,
                language = langCode,
                imageUri = imageToAttach,
                modelTag = "Boss"
            )
            chatRepository.saveMessage(userMsg)

            _isThinking.value = true

            try {
                val memories = memoryRepository.getMemoriesSnapshot()

                val response = aiEngine.processQuery(
                    prompt = query,
                    settings = currentSettings,
                    memories = memories,
                    attachedImageUri = imageToAttach
                )

                val aiMsg = ChatMessage(
                    sender = MessageSender.AI,
                    content = response.content,
                    isOnline = response.isOnline,
                    language = response.language,
                    modelTag = response.modelTag,
                    contentBn = response.contentBn,
                    contentBanglish = response.contentBanglish,
                    isBossStrategyProposal = response.isBossStrategyProposal,
                    strategyActionId = response.strategyActionId
                )
                chatRepository.saveMessage(aiMsg)

                if (currentSettings.autoSpeakResponses) {
                    voiceController.speak(
                        text = response.content,
                        languageCode = response.language,
                        rate = currentSettings.speechRate,
                        pitch = currentSettings.speechPitch
                    )
                }
            } catch (e: Exception) {
                val errorMsg = ChatMessage(
                    sender = MessageSender.SYSTEM,
                    content = "Error processing request: ${e.localizedMessage ?: "Unknown error"}",
                    isOnline = false,
                    language = "en",
                    modelTag = "System"
                )
                chatRepository.saveMessage(errorMsg)
            } finally {
                _isThinking.value = false
            }
        }
    }

    fun executeBossStrategy(strategyId: String?, customTitle: String? = null) {
        if (!creditRepository.hasCredits(3)) {
            _showCreditDialog.value = true
            return
        }

        creditRepository.deductCredits(3)
        creditRepository.recordStrategyCompleted()

        val title = customTitle ?: "Autonomous Freemium Growth & Self-Healing Pipeline"
        selfHealingCoordinator.startAutonomousTask(
            titleEn = title,
            titleBn = "অটোনোমাস ফ্রিমিয়াম গ্রোথ ও স্ব-নিরাময় পাইপলাইন",
            titleBanglish = "Autonomous Freemium Growth & Self-Healing Pipeline",
            description = "Executing multi-step company growth workflow with live self-healing error detection.",
            simulateSelfHealing = true
        )
        _showSelfHealingModal.value = true

        viewModelScope.launch {
            val systemConfirmation = ChatMessage(
                sender = MessageSender.SYSTEM,
                content = "🚀 Boss confirmed strategy execution! Autonomous task pipeline activated with real-time self-healing monitors.",
                isOnline = true,
                language = "en",
                modelTag = "Master Agent"
            )
            chatRepository.saveMessage(systemConfirmation)
        }
    }

    fun confirmSelfHealingFix() {
        selfHealingCoordinator.confirmSelfHealingFix()
    }

    fun cancelAutonomousTask() {
        selfHealingCoordinator.cancelTask()
    }

    fun dismissAutonomousTask() {
        selfHealingCoordinator.dismissTask()
        _showSelfHealingModal.value = false
    }

    fun speakMessage(text: String, lang: String = "en") {
        val currentSettings = settings.value
        voiceController.speak(
            text = text,
            languageCode = lang,
            rate = currentSettings.speechRate,
            pitch = currentSettings.speechPitch
        )
    }

    fun stopSpeaking() {
        voiceController.stop()
    }

    fun clearChat() {
        viewModelScope.launch {
            chatRepository.clearHistory()
        }
    }

    class Factory(
        private val chatRepository: ChatRepository,
        private val memoryRepository: MemoryRepository,
        private val settingsRepository: SettingsRepository,
        private val aiEngine: AIAgentEngine,
        private val voiceController: VoiceController,
        private val creditRepository: CreditRepository,
        private val selfHealingCoordinator: SelfHealingTaskCoordinator,
        private val businessRepository: BusinessRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ChatViewModel(
                chatRepository,
                memoryRepository,
                settingsRepository,
                aiEngine,
                voiceController,
                creditRepository,
                selfHealingCoordinator,
                businessRepository
            ) as T
        }
    }
}
