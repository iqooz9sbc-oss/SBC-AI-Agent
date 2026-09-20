package com.example.ui.screens

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.GlowPurple
import com.example.ui.viewmodel.VoiceState
import com.example.ui.viewmodel.VoiceViewModel

@Composable
fun VoiceScreen(
    viewModel: VoiceViewModel,
    isBn: Boolean = false,
    modifier: Modifier = Modifier
) {
    val voiceState by viewModel.voiceState.collectAsStateWithLifecycle()
    val transcript by viewModel.transcript.collectAsStateWithLifecycle()
    val lastAiResponse by viewModel.lastAiResponse.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    var rateSlider by remember(settings.speechRate) { mutableFloatStateOf(settings.speechRate) }
    var pitchSlider by remember(settings.speechPitch) { mutableFloatStateOf(settings.speechPitch) }

    // Speech Recognizer Intent Launcher
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spoken = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull() ?: ""
            viewModel.onSpeechResult(spoken)
        }
    }

    // Record audio permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, if (isBn) "bn-BD" else "en-US")
                putExtra(RecognizerIntent.EXTRA_PROMPT, if (isBn) "কিছু বলুন..." else "Say something...")
            }
            try {
                speechLauncher.launch(intent)
            } catch (e: Exception) {
                // Speech recognizer not installed, fallback to demo phrase
                viewModel.startListeningSimulation(if (isBn) "আমার আজকের শিডিউল কি?" else "What can you do?")
            }
        } else {
            // Permission denied, run simulation
            viewModel.startListeningSimulation(if (isBn) "কেমন আছো?" else "Hello Personal AI!")
        }
    }

    // Animation for glowing orb
    val infiniteTransition = rememberInfiniteTransition(label = "voice_orb")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (voiceState == VoiceState.SPEAKING || voiceState == VoiceState.LISTENING) 1.25f else 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("voice_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Voice Mode Header
        Text(
            text = if (isBn) "ভয়েস ইন্টারেকশন ও টিটিএস" else "Voice AI & Speech Studio",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = if (isBn) {
                "ইংরেজি ও বাংলা দ্বিভাষিক ভয়েস সাপোর্ট এবং রিয়েল-টাইম স্পিচ রেসপন্স"
            } else {
                "Bilingual Speech-to-Text & Text-to-Speech synthesis in English & Bangla"
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Center Voice Pulse Orb
        Box(
            modifier = Modifier
                .size(180.dp),
            contentAlignment = Alignment.Center
        ) {
            // Outer pulse circle
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                when (voiceState) {
                                    VoiceState.LISTENING -> CyberCyan.copy(alpha = 0.35f)
                                    VoiceState.SPEAKING -> GlowPurple.copy(alpha = 0.45f)
                                    VoiceState.PROCESSING -> ElectricViolet.copy(alpha = 0.4f)
                                    VoiceState.IDLE -> ElectricViolet.copy(alpha = 0.15f)
                                },
                                Color.Transparent
                            )
                        )
                    )
            )

            // Inner main mic orb button
            Surface(
                onClick = {
                    if (voiceState == VoiceState.SPEAKING) {
                        viewModel.stopSpeaking()
                    } else {
                        permissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                    }
                },
                shape = CircleShape,
                color = when (voiceState) {
                    VoiceState.LISTENING -> CyberCyan
                    VoiceState.SPEAKING -> GlowPurple
                    VoiceState.PROCESSING -> ElectricViolet
                    VoiceState.IDLE -> ElectricViolet
                },
                modifier = Modifier
                    .size(96.dp)
                    .testTag("voice_orb_button"),
                shadowElevation = 8.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (voiceState == VoiceState.SPEAKING) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = "Voice Action",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Current status label
        Text(
            text = when (voiceState) {
                VoiceState.IDLE -> if (isBn) "কথা বলতে মাইকে চাপ দিন" else "Tap microphone to speak"
                VoiceState.LISTENING -> if (isBn) "শুনছি... কথা বলুন" else "Listening to your voice..."
                VoiceState.PROCESSING -> if (isBn) "ভয়েস প্রসেসিং হচ্ছে..." else "Processing query..."
                VoiceState.SPEAKING -> if (isBn) "উত্তর বলছি... (থামাতে ট্যাপ করুন)" else "Speaking response... (Tap to stop)"
            },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = when (voiceState) {
                VoiceState.LISTENING -> CyberCyan
                VoiceState.SPEAKING -> GlowPurple
                else -> MaterialTheme.colorScheme.onSurface
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Simulated Voice Testing Chips (Ideal for emulator testing without mic)
        Text(
            text = if (isBn) "দ্রুত ভয়েস টেস্ট প্রম্পট:" else "Quick Voice Test Prompts:",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            OutlinedButton(
                onClick = { viewModel.startListeningSimulation("What is my name?") },
                modifier = Modifier.padding(horizontal = 4.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text("“What is my name?”", fontSize = 12.sp)
            }
            OutlinedButton(
                onClick = { viewModel.startListeningSimulation("কেমন আছো?") },
                modifier = Modifier.padding(horizontal = 4.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text("“কেমন আছো?”", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Transcript Card
        if (transcript.isNotBlank()) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = if (isBn) "🎙️ আপনার উচ্চারিত কথা:" else "🎙️ Spoken Transcript:",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyberCyan,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = transcript,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Last AI Voice Response Card
        if (lastAiResponse.isNotBlank()) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isBn) "🔊 এআই ভয়েস উত্তর:" else "🔊 AI Spoken Response:",
                            style = MaterialTheme.typography.labelSmall,
                            color = GlowPurple,
                            fontWeight = FontWeight.SemiBold
                        )
                        IconButton(
                            onClick = {
                                viewModel.testSpeak(lastAiResponse, if (isBn) "bn" else "en")
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "Replay",
                                tint = ElectricViolet,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = lastAiResponse,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 22.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Voice Controls & TTS Sliders Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (isBn) "টেক্সট-টু-স্পিচ (TTS) নিয়ন্ত্রণ" else "Text-to-Speech (TTS) Tuning",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Speech Rate
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isBn) "বক্তৃতার গতি (Speed Rate)" else "Speech Rate",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "%.1fx".format(rateSlider),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Slider(
                    value = rateSlider,
                    onValueChange = {
                        rateSlider = it
                        viewModel.updateSpeechParams(rateSlider, pitchSlider)
                    },
                    valueRange = 0.5f..2.0f,
                    colors = SliderDefaults.colors(thumbColor = ElectricViolet, activeTrackColor = ElectricViolet)
                )

                // Speech Pitch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isBn) "ভয়েস পিচ (Pitch)" else "Voice Pitch",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "%.1fx".format(pitchSlider),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Slider(
                    value = pitchSlider,
                    onValueChange = {
                        pitchSlider = it
                        viewModel.updateSpeechParams(rateSlider, pitchSlider)
                    },
                    valueRange = 0.5f..2.0f,
                    colors = SliderDefaults.colors(thumbColor = CyberCyan, activeTrackColor = CyberCyan)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Test Voice Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.testSpeak(
                                text = "Hello! Personal AI voice synthesis is functioning properly.",
                                languageCode = "en"
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Test EN", fontSize = 13.sp)
                    }

                    Button(
                        onClick = {
                            viewModel.testSpeak(
                                text = "নমস্কার! পার্সোনাল এআই সহকারী সফলভাবে কথা বলছে।",
                                languageCode = "bn"
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Black)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Test বাংলা", fontSize = 13.sp, color = Color.Black)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}
