import com.aiassistant.app.features.headshot.HeadshotGeneratorScreen
import com.aiassistant.app.features.headshot.GeminiHeadshotRepository
composable("headshot_generator") {
    val headshotRepository = remember { 
        GeminiHeadshotRepository(apiKey = "YOUR_GEMINI_API_KEY") 
    }

    HeadshotGeneratorScreen(
        creditViewModel = creditViewModel,
        repository = headshotRepository,
        onNavigateToPaywall = { 
            // Paywall নেভিগেশন
        },
        onBack = { 
            // Back নেভিগেশন
        }
    )
}
package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.AppLanguage
import com.example.ui.components.AgentBottomNav
import com.example.ui.components.AgentTopAppBar
import com.example.ui.screens.AutomationScreen
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.MemoryScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.VisionScreen
import com.example.ui.screens.VoiceScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AutomationViewModel
import com.example.ui.viewmodel.ChatViewModel
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.MemoryViewModel
import com.example.ui.viewmodel.NavTab
import com.example.ui.viewmodel.SettingsViewModel
import com.example.ui.viewmodel.VisionViewModel
import com.example.ui.viewmodel.VoiceViewModel

class MainActivity : ComponentActivity() {

    private val app by lazy { application as PersonalAIApplication }

    private val mainViewModel by viewModels<MainViewModel> {
        MainViewModel.Factory(app.settingsRepository)
    }

    private val chatViewModel by viewModels<ChatViewModel> {
        ChatViewModel.Factory(
            app.chatRepository,
            app.memoryRepository,
            app.settingsRepository,
            app.aiEngine,
            app.voiceController,
            app.creditRepository,
            app.selfHealingCoordinator,
            app.businessRepository
        )
    }

    private val memoryViewModel by viewModels<MemoryViewModel> {
        MemoryViewModel.Factory(app.memoryRepository)
    }

    private val voiceViewModel by viewModels<VoiceViewModel> {
        VoiceViewModel.Factory(
            app.voiceController,
            app.settingsRepository,
            app.memoryRepository,
            app.aiEngine
        )
    }

    private val visionViewModel by viewModels<VisionViewModel> {
        VisionViewModel.Factory(
            app.settingsRepository,
            app.aiEngine
        )
    }

    private val automationViewModel by viewModels<AutomationViewModel> {
        AutomationViewModel.Factory(
            app.automationRepository,
            app.settingsRepository,
            app.businessRepository,
            app.selfHealingCoordinator,
            app.creditRepository
        )
    }

    private val settingsViewModel by viewModels<SettingsViewModel> {
        SettingsViewModel.Factory(
            app.settingsRepository,
            app.chatRepository,
            app.memoryRepository,
            app.creditRepository
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                PersonalAIAgentApp(
                    mainViewModel = mainViewModel,
                    chatViewModel = chatViewModel,
                    memoryViewModel = memoryViewModel,
                    voiceViewModel = voiceViewModel,
                    visionViewModel = visionViewModel,
                    automationViewModel = automationViewModel,
                    settingsViewModel = settingsViewModel
                )
            }
        }
    }
}

@Composable
fun PersonalAIAgentApp(
    mainViewModel: MainViewModel,
    chatViewModel: ChatViewModel,
    memoryViewModel: MemoryViewModel,
    voiceViewModel: VoiceViewModel,
    visionViewModel: VisionViewModel,
    automationViewModel: AutomationViewModel,
    settingsViewModel: SettingsViewModel
) {
    val currentTab by mainViewModel.currentTab.collectAsStateWithLifecycle()
    val settings by mainViewModel.settings.collectAsStateWithLifecycle()

    val isBn = settings.selectedLanguage == AppLanguage.BANGLA

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            AgentTopAppBar(
                settings = settings,
                onToggleOnline = { mainViewModel.toggleOnlineMode() },
                onToggleLanguage = { mainViewModel.toggleLanguage() }
            )
        },
        bottomBar = {
            AgentBottomNav(
                currentTab = currentTab,
                onTabSelected = { mainViewModel.selectTab(it) },
                isBn = isBn
            )
        }
    ) { innerPadding ->
        Crossfade(
            targetState = currentTab,
            animationSpec = tween(220),
            modifier = Modifier.padding(innerPadding),
            label = "tab_crossfade"
        ) { tab ->
            when (tab) {
                NavTab.CHAT -> ChatScreen(
                    viewModel = chatViewModel,
                    onVoiceRequest = { mainViewModel.selectTab(NavTab.VOICE) }
                )
                NavTab.MEMORY -> MemoryScreen(
                    viewModel = memoryViewModel,
                    isBn = isBn
                )
                NavTab.VOICE -> VoiceScreen(
                    viewModel = voiceViewModel,
                    isBn = isBn
                )
                NavTab.VISION -> VisionScreen(
                    viewModel = visionViewModel,
                    isBn = isBn
                )
                NavTab.AUTOMATION -> AutomationScreen(
                    viewModel = automationViewModel,
                    isBn = isBn
                )
                NavTab.SETTINGS -> SettingsScreen(
                    viewModel = settingsViewModel,
                    isBn = isBn
                )
            }
        }
    }
}
