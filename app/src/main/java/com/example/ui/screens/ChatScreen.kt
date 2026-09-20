package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.domain.model.AppLanguage
import com.example.domain.model.AutonomousTask
import com.example.domain.model.ChatMessage
import com.example.domain.model.MessageSender
import com.example.domain.model.TaskStatus
import com.example.ui.components.CreditStoreDialog
import com.example.ui.components.SelfHealingTaskDialog
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GlowPurple
import com.example.ui.viewmodel.ChatViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier,
    onVoiceRequest: () -> Unit = {}
) {
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val inputText by viewModel.inputText.collectAsStateWithLifecycle()
    val isThinking by viewModel.isThinking.collectAsStateWithLifecycle()
    val attachedImageUri by viewModel.attachedImageUri.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val creditProfile by viewModel.creditProfile.collectAsStateWithLifecycle()
    val currentTask by viewModel.currentAutonomousTask.collectAsStateWithLifecycle()
    val showCreditDialog by viewModel.showCreditDialog.collectAsStateWithLifecycle()
    val showSelfHealingModal by viewModel.showSelfHealingModal.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val listState = rememberLazyListState()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        viewModel.attachImage(uri?.toString())
    }

    LaunchedEffect(messages.size, isThinking) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val isBn = settings.selectedLanguage == AppLanguage.BANGLA

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("chat_screen")
    ) {
        // Executive Status & Credits Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (settings.isOnlineMode) EmeraldSuccess else Color(0xFFF97316))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (settings.isOnlineMode) {
                        if (isBn) "অনলাইন মোড (ক্লাউড জেমিনাই)" else "Online Mode (Gemini Cloud)"
                    } else {
                        if (isBn) "অফলাইন মোড (লোকাল ইঞ্জিন)" else "Offline Mode (Local Engine)"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Credit Balance Pill Button
            Surface(
                onClick = { viewModel.openCreditStore(true) },
                shape = RoundedCornerShape(16.dp),
                color = ElectricViolet.copy(alpha = 0.15f),
                border = BorderStroke(1.dp, ElectricViolet.copy(alpha = 0.4f)),
                modifier = Modifier.testTag("credit_balance_pill")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = "Credits",
                        tint = ElectricViolet,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${creditProfile.credits} AI Credits",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = ElectricViolet
                    )
                }
            }

            if (messages.isNotEmpty()) {
                IconButton(
                    onClick = { viewModel.clearChat() },
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("clear_chat_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = "Clear Chat History",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Active Autonomous Task Live Banner
        if (currentTask != null) {
            val task = currentTask!!
            val isTaskError = task.status == TaskStatus.ERROR_DETECTED
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isTaskError) Color(0xFFFEF2F2) else ElectricViolet.copy(alpha = 0.12f)
                ),
                border = BorderStroke(1.dp, if (isTaskError) Color(0xFFEF4444) else ElectricViolet),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .testTag("active_task_banner")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isTaskError) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                        } else if (task.status == TaskStatus.COMPLETED) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(18.dp))
                        } else {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = ElectricViolet)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (isBn) task.titleBn else task.titleEn,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Text(
                                text = if (isTaskError) "⚠️ Barrier Detected: Self-Healing Ready" else "Step ${task.currentStepIndex + 1}/${task.steps.size} • ${task.status.name}",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isTaskError) Color(0xFFDC2626) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Button(
                        onClick = { viewModel.openSelfHealingModal(true) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isTaskError) Color(0xFFDC2626) else ElectricViolet
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text(
                            text = if (isTaskError) (if (isBn) "সমাধান করুন" else "Fix Now") else (if (isBn) "মনিটর" else "View"),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Messages List or Empty State
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (messages.isEmpty()) {
                ChatEmptyGreeting(
                    isBn = isBn,
                    onPromptSelected = { prompt -> viewModel.sendMessage(prompt) }
                )
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp)
                ) {
                    items(messages, key = { it.id }) { message ->
                        ChatMessageBubble(
                            message = message,
                            onCopy = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("AI Message", message.content)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, if (isBn) "কপি করা হয়েছে" else "Copied to clipboard", Toast.LENGTH_SHORT).show()
                            },
                            onSpeak = {
                                viewModel.speakMessage(message.content, message.language)
                            },
                            onExecuteStrategy = { actionId ->
                                viewModel.executeBossStrategy(actionId)
                            },
                            onTestSelfHealing = {
                                viewModel.executeBossStrategy(null, "Autonomous Self-Healing Task Pipeline")
                            }
                        )
                    }

                    if (isThinking) {
                        item {
                            ThinkingBubble(isBn = isBn)
                        }
                    }
                }
            }
        }

        // Quick Suggestions Row
        AnimatedVisibility(
            visible = !isThinking,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(viewModel.quickPrompts) { prompt ->
                    Surface(
                        onClick = {
                            val text = when (settings.selectedLanguage) {
                                AppLanguage.BANGLA -> prompt.queryBn
                                AppLanguage.BANGLISH -> prompt.queryBanglish
                                else -> prompt.queryEn
                            }
                            viewModel.sendMessage(text)
                        },
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.testTag("quick_prompt_${prompt.titleEn}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = CyberCyan,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = when (settings.selectedLanguage) {
                                    AppLanguage.BANGLA -> prompt.titleBn
                                    AppLanguage.BANGLISH -> prompt.titleBanglish
                                    else -> prompt.titleEn
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // Image Attachment Preview
        if (attachedImageUri != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = attachedImageUri,
                            contentDescription = "Attached image",
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isBn) "ছবি সংযুক্ত" else "Image attached",
                            style = MaterialTheme.typography.labelSmall
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = { viewModel.attachImage(null) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove attached image",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // Input Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 3.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier.testTag("attach_image_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = "Attach image",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                OutlinedTextField(
                    value = inputText,
                    onValueChange = { viewModel.onInputChanged(it) },
                    placeholder = {
                        Text(
                            text = if (isBn) "বস, বার্তা লিখুন (বাংলা, English বা বাংলিশ)..." else "Ask Master Agent (English, বাংলা, Banglish)...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp)
                        .testTag("chat_input_field"),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricViolet,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    ),
                    maxLines = 4
                )

                if (inputText.isNotBlank() || attachedImageUri != null) {
                    IconButton(
                        onClick = { viewModel.sendMessage() },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("send_message_button")
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = ElectricViolet,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Send message",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                } else {
                    IconButton(
                        onClick = onVoiceRequest,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("voice_input_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Voice input",
                            tint = CyberCyan
                        )
                    }
                }
            }
        }
    }

    // Dialogs
    if (showCreditDialog) {
        CreditStoreDialog(
            creditProfile = creditProfile,
            packages = listOf(
                com.example.domain.model.CreditPackage("pkg_starter", "Starter Boost", "স্টার্টার বুস্ট", 100, "$1.99", "৳১৯৯", false),
                com.example.domain.model.CreditPackage("pkg_growth", "Executive Growth Pack", "এক্সিকিউটিভ প্যাক", 500, "$4.99", "৳৪৯৯", true),
                com.example.domain.model.CreditPackage("pkg_power", "Autonomous Powerhouse", "মাস্টার পাওয়ার প্যাক", 2000, "$14.99", "৳১,৪৯৯", false)
            ),
            isBn = isBn,
            onDismiss = { viewModel.openCreditStore(false) },
            onPurchasePackage = { pkg ->
                // Handled in SettingsViewModel / repo
                viewModel.openCreditStore(false)
            },
            onUpgradeSubscription = { tier ->
                viewModel.openCreditStore(false)
            },
            onToggleCurrency = {}
        )
    }

    if (showSelfHealingModal && currentTask != null) {
        SelfHealingTaskDialog(
            task = currentTask!!,
            isBn = isBn,
            onDismiss = { viewModel.dismissAutonomousTask() },
            onConfirmFix = { viewModel.confirmSelfHealingFix() },
            onCancelTask = { viewModel.cancelAutonomousTask() }
        )
    }
}

@Composable
fun ChatMessageBubble(
    message: ChatMessage,
    onCopy: () -> Unit,
    onSpeak: () -> Unit,
    onExecuteStrategy: (String?) -> Unit = {},
    onTestSelfHealing: () -> Unit = {}
) {
    val isUser = message.sender == MessageSender.USER
    val isSystem = message.sender == MessageSender.SYSTEM
    val timeFormatted = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(message.timestamp))

    // Language view state for multi-lingual output in single interface
    var selectedLanguageView by remember {
        mutableStateOf(message.language)
    }

    val displayContent = when (selectedLanguageView) {
        "bn" -> message.contentBn ?: message.content
        "banglish" -> message.contentBanglish ?: message.content
        else -> message.content
    }

    if (isSystem) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
        }
        return
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        if (!isUser) {
            Surface(
                shape = CircleShape,
                color = ElectricViolet,
                modifier = Modifier
                    .size(32.dp)
                    .padding(top = 4.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI Agent",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = 320.dp)
        ) {
            // Model tag
            if (!isUser) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 2.dp)
                ) {
                    Text(
                        text = message.modelTag,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (message.isOnline) CyberCyan else Color(0xFFF97316),
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = timeFormatted,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            Card(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isUser) 16.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 16.dp
                ),
                colors = CardDefaults.cardColors(
                    containerColor = if (isUser) ElectricViolet else MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    if (message.imageUri != null) {
                        AsyncImage(
                            model = message.imageUri,
                            contentDescription = "Sent image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .padding(bottom = 8.dp)
                        )
                    }

                    // Multi-lingual Language Selector Pills (EN / বাং / Banglish)
                    if (!isUser && (message.contentBn != null || message.contentBanglish != null)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Surface(
                                onClick = { selectedLanguageView = "en" },
                                shape = RoundedCornerShape(6.dp),
                                color = if (selectedLanguageView == "en") ElectricViolet else MaterialTheme.colorScheme.surface,
                                modifier = Modifier.height(24.dp)
                            ) {
                                Text(
                                    text = "🇬🇧 EN",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedLanguageView == "en") Color.White else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }

                            if (message.contentBn != null) {
                                Surface(
                                    onClick = { selectedLanguageView = "bn" },
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (selectedLanguageView == "bn") ElectricViolet else MaterialTheme.colorScheme.surface,
                                    modifier = Modifier.height(24.dp)
                                ) {
                                    Text(
                                        text = "🇧🇩 বাং",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selectedLanguageView == "bn") Color.White else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            if (message.contentBanglish != null) {
                                Surface(
                                    onClick = { selectedLanguageView = "banglish" },
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (selectedLanguageView == "banglish") ElectricViolet else MaterialTheme.colorScheme.surface,
                                    modifier = Modifier.height(24.dp)
                                ) {
                                    Text(
                                        text = "🔤 Banglish",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selectedLanguageView == "banglish") Color.White else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }

                    Text(
                        text = displayContent,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface,
                        lineHeight = 22.sp
                    )

                    // Boss Strategy Confirmation Card
                    if (message.isBossStrategyProposal) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.5.dp, CyberCyan),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("boss_strategy_proposal_card")
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("👑", fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Boss, should I execute this strategy?",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = CyberCyan
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Button(
                                        onClick = { onExecuteStrategy(message.strategyActionId) },
                                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(32.dp)
                                            .testTag("execute_strategy_now_button")
                                    ) {
                                        Text("🚀 Execute Now", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    OutlinedButton(
                                        onClick = onTestSelfHealing,
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(32.dp)
                                            .testTag("test_self_healing_button")
                                    ) {
                                        Text("🛠️ Self-Healing", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    // Action buttons for AI messages
                    if (!isUser) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = onCopy,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy message",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                            IconButton(
                                onClick = onSpeak,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = "Speak message",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            if (isUser) {
                Text(
                    text = timeFormatted,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 2.dp, end = 4.dp)
                )
            }
        }

        if (isUser) {
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .size(32.dp)
                    .padding(top = 4.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "User",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ThinkingBubble(isBn: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = ElectricViolet,
            modifier = Modifier.size(32.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = CyberCyan
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (isBn) "বস, বিশ্লেষণ করছি এবং স্ব-নিরাময় পথ প্রস্তুত করছি..." else "Boss, analyzing requirements and optimizing workflows...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ChatEmptyGreeting(
    isBn: Boolean,
    onPromptSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = ElectricViolet.copy(alpha = 0.15f),
            modifier = Modifier.size(80.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = ElectricViolet,
                    modifier = Modifier.size(44.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (isBn) "স্বাগতম, বস! অটোনোমাস মাস্টার এআই" else "Greetings Boss! Autonomous Master AI",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = if (isBn) {
                "ব্যবসায়িক প্রতিবেদন, স্ব-নিরাময় টাস্ক, ফিনান্সিয়াল অ্যাকাউন্টিং এবং ইংরেজি, বাংলা ও বাংলিশে সাবলীল সমাধান।"
            } else {
                "Executive business reports, self-healing multi-step task execution, financial accounting, and trilingual fluency (English, বাংলা, Banglish)."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = if (isBn) "নিচের যেকোনো বিষয়ে শুরু করুন:" else "Select an autonomous workflow:",
            style = MaterialTheme.typography.labelMedium,
            color = CyberCyan,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(12.dp))

        val starterPrompts = if (isBn) {
            listOf(
                "বস, আজকের কোম্পানি রিপোর্ট এবং কৌশলগত পরামর্শ দিন।",
                "একটি জিরো-বাজেট ভাইরাল মার্কেটিং কৌশল প্রস্তাব করুন এবং বাস্তবায়নের অনুমতি চান।",
                "সিস্টেমের স্ব-নিরাময় ক্ষমতা যাচাইয়ের জন্য একটি অটোনোমাস মাল্টি-স্টেপ টাস্ক চালান।"
            )
        } else {
            listOf(
                "Give me today's daily executive company report and strategic recommendations, Boss.",
                "Recommend a zero-budget viral marketing strategy and ask me for confirmation.",
                "Run an autonomous self-healing multi-step workflow to verify system recovery."
            )
        }

        starterPrompts.forEach { prompt ->
            Card(
                onClick = { onPromptSelected(prompt) },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = ElectricViolet,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = prompt,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
