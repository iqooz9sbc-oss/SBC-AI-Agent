package com.example

import android.app.Application
import com.example.data.local.AppDatabase
import com.example.data.repository.AutomationRepository
import com.example.data.repository.BusinessRepository
import com.example.data.repository.ChatRepository
import com.example.data.repository.CreditRepository
import com.example.data.repository.MemoryRepository
import com.example.data.repository.SettingsRepository
import com.example.engine.AIAgentEngine
import com.example.engine.SelfHealingTaskCoordinator
import com.example.voice.VoiceController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class PersonalAIApplication : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var chatRepository: ChatRepository
        private set

    lateinit var memoryRepository: MemoryRepository
        private set

    lateinit var automationRepository: AutomationRepository
        private set

    lateinit var settingsRepository: SettingsRepository
        private set

    lateinit var creditRepository: CreditRepository
        private set

    lateinit var businessRepository: BusinessRepository
        private set

    lateinit var selfHealingCoordinator: SelfHealingTaskCoordinator
        private set

    lateinit var aiEngine: AIAgentEngine
        private set

    lateinit var voiceController: VoiceController
        private set

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getInstance(this)
        chatRepository = ChatRepository(database.chatMessageDao())
        memoryRepository = MemoryRepository(database.memoryDao())
        automationRepository = AutomationRepository(database.automationDao())
        settingsRepository = SettingsRepository(this)
        creditRepository = CreditRepository(this)
        businessRepository = BusinessRepository()
        selfHealingCoordinator = SelfHealingTaskCoordinator()
        aiEngine = AIAgentEngine()
        voiceController = VoiceController(this)

        // Prepopulate default automations if none exist
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val rules = automationRepository.getRules().first()
                automationRepository.seedDefaultsIfEmpty(rules.size)
            } catch (e: Exception) {
                // Initial creation handling
            }
        }
    }

    override fun onTerminate() {
        voiceController.shutdown()
        super.onTerminate()
    }
}
