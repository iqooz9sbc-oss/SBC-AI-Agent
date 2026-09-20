package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.AutonomousTask
import com.example.domain.model.StepStatus
import com.example.domain.model.TaskStatus
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GlowPurple

@Composable
fun SelfHealingTaskDialog(
    task: AutonomousTask,
    isBn: Boolean = false,
    onDismiss: () -> Unit,
    onConfirmFix: () -> Unit,
    onCancelTask: () -> Unit
) {
    val isError = task.status == TaskStatus.ERROR_DETECTED
    val isCompleted = task.status == TaskStatus.COMPLETED
    val isHealing = task.status == TaskStatus.HEALING
    val isRunning = task.status == TaskStatus.RUNNING

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = when {
                            isError -> Color(0xFFEF4444).copy(alpha = 0.2f)
                            isCompleted -> EmeraldSuccess.copy(alpha = 0.2f)
                            else -> ElectricViolet.copy(alpha = 0.2f)
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = when {
                                    isError -> Icons.Default.Warning
                                    isCompleted -> Icons.Default.CheckCircle
                                    isHealing -> Icons.Default.Build
                                    else -> Icons.Default.Psychology
                                },
                                contentDescription = null,
                                tint = when {
                                    isError -> Color(0xFFEF4444)
                                    isCompleted -> EmeraldSuccess
                                    isHealing -> CyberCyan
                                    else -> ElectricViolet
                                },
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isBn) task.titleBn else task.titleEn,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            text = when (task.status) {
                                TaskStatus.IDLE -> if (isBn) "প্রস্তুত" else "Idle"
                                TaskStatus.RUNNING -> if (isBn) "স্বয়ংক্রিয়ভাবে চলছে..." else "Running Autonomous Steps..."
                                TaskStatus.ERROR_DETECTED -> if (isBn) "⚠️ ত্রুটি শনাক্ত — স্ব-নিরাময় প্রস্তুত" else "⚠️ Barrier Detected — Self-Healing Diagnostic Ready"
                                TaskStatus.HEALING -> if (isBn) "🛠️ সমাধান প্রয়োগ করা হচ্ছে..." else "🛠️ Applying Fix & Resuming..."
                                TaskStatus.COMPLETED -> if (isBn) "✅ সফলভাবে সম্পন্ন হয়েছে!" else "✅ Task Completed Successfully"
                                TaskStatus.CANCELLED -> if (isBn) "বাতিল করা হয়েছে" else "Cancelled by Boss"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = when {
                                isError -> Color(0xFFEF4444)
                                isCompleted -> EmeraldSuccess
                                isHealing -> CyberCyan
                                else -> ElectricViolet
                            },
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Section 0: Visual Progress Tracking Bar (Successful vs Healed vs Barrier)
                val sCount = task.steps.count { it.status == StepStatus.COMPLETED }
                val hCount = task.steps.count { it.status == StepStatus.HEALED }
                val bCount = task.steps.count { it.status == StepStatus.FAILED }
                val totalSteps = task.steps.size.coerceAtLeast(1)

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isBn) "ভিজুয়াল এক্সিকিউশন ট্র্যাকিং" else "Visual Execution Tracking",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = CyberCyan
                            )
                            Text(
                                text = "✓ $sCount OK  •  ★ $hCount Healed  •  ! $bCount Barrier",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        // Mini stacked bar
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxSize()) {
                                if (sCount > 0) {
                                    Box(
                                        modifier = Modifier
                                            .weight(sCount.toFloat())
                                            .fillMaxSize()
                                            .background(EmeraldSuccess)
                                    )
                                }
                                if (hCount > 0) {
                                    Box(
                                        modifier = Modifier
                                            .weight(hCount.toFloat())
                                            .fillMaxSize()
                                            .background(CyberCyan)
                                    )
                                }
                                if (bCount > 0) {
                                    Box(
                                        modifier = Modifier
                                            .weight(bCount.toFloat())
                                            .fillMaxSize()
                                            .background(Color(0xFFEF4444))
                                    )
                                }
                                val remaining = totalSteps - sCount - hCount - bCount
                                if (remaining > 0) {
                                    Box(
                                        modifier = Modifier
                                            .weight(remaining.toFloat())
                                            .fillMaxSize()
                                            .background(Color.Transparent)
                                    )
                                }
                            }
                        }
                    }
                }

                // Section 1: Step-by-Step Progress Pipeline
                Text(
                    text = if (isBn) "টাস্ক এক্সিকিউশন পাইপলাইন:" else "Task Execution Pipeline:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                task.steps.forEachIndexed { index, step ->
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = when (step.status) {
                                StepStatus.COMPLETED -> EmeraldSuccess.copy(alpha = 0.08f)
                                StepStatus.HEALED -> CyberCyan.copy(alpha = 0.12f)
                                StepStatus.FAILED -> Color(0xFFEF4444).copy(alpha = 0.12f)
                                StepStatus.ACTIVE -> ElectricViolet.copy(alpha = 0.12f)
                                StepStatus.PENDING -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            }
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            when (step.status) {
                                StepStatus.COMPLETED -> Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(18.dp))
                                StepStatus.HEALED -> Icon(Icons.Default.Security, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(18.dp))
                                StepStatus.FAILED -> Icon(Icons.Default.Error, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                                StepStatus.ACTIVE -> CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = ElectricViolet)
                                StepStatus.PENDING -> Icon(Icons.Default.PlayArrow, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f), modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${index + 1}. " + (if (isBn) step.titleBn else step.titleEn),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = if (step.status == StepStatus.ACTIVE || step.status == StepStatus.FAILED) FontWeight.Bold else FontWeight.Normal,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = when (step.status) {
                                    StepStatus.COMPLETED -> if (isBn) "সম্পন্ন" else "Done"
                                    StepStatus.HEALED -> if (isBn) "নিরাময়কৃত" else "Healed"
                                    StepStatus.FAILED -> if (isBn) "ব্যর্থ" else "Barrier"
                                    StepStatus.ACTIVE -> if (isBn) "চলছে..." else "Active"
                                    StepStatus.PENDING -> if (isBn) "অপেক্ষমান" else "Queued"
                                },
                                fontSize = 10.sp,
                                color = when (step.status) {
                                    StepStatus.COMPLETED -> EmeraldSuccess
                                    StepStatus.HEALED -> CyberCyan
                                    StepStatus.FAILED -> Color(0xFFEF4444)
                                    StepStatus.ACTIVE -> ElectricViolet
                                    StepStatus.PENDING -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                },
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Section 2: Self-Healing Trigger & Confirmation Card (CRITICAL)
                if (isError && task.requiresBossConfirmation) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFEF4444)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("self_healing_confirmation_card")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isBn) "স্ব-নিরাময় এআই ডায়াগনস্টিক" else "Self-Healing AI Diagnostic",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF991B1B)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = task.errorDetails ?: "API Schema mismatch detected.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF7F1D1D),
                                fontSize = 12.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Surface(
                                color = Color.White,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = if (isBn) "🛠️ প্রস্তাবিত সমাধান:" else "🛠️ Suggested Solution:",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E293B)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = (if (isBn) task.suggestedSolutionBn else task.suggestedSolutionEn)
                                            ?: "Auto-patch schema validation and retry with exponential backoff.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF334155),
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = if (isBn) task.bossQuestionBn else task.bossQuestionEn,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = onConfirmFix,
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("confirm_self_healing_button")
                                ) {
                                    Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(if (isBn) "সমাধান প্রয়োগ করুন" else "Apply Fix & Resume", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = onCancelTask,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(38.dp)
                                ) {
                                    Text(if (isBn) "বাতিল" else "Cancel", fontSize = 11.sp, color = Color(0xFFDC2626))
                                }
                            }
                        }
                    }
                }

                // Section 3: Task Completion Summary
                if (isCompleted) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = EmeraldSuccess.copy(alpha = 0.12f)),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, EmeraldSuccess),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isBn) "টাস্ক সম্পূর্ণ ও সফল!" else "Task Completed Successfully!",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldSuccess
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = (if (isBn) task.resultSummaryBn else task.resultSummaryEn)
                                    ?: "All objectives accomplished with self-healing verified.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Section 4: Live Execution Terminal Logs
                Surface(
                    color = Color(0xFF0F172A),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Terminal Logs:",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = CyberCyan,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        task.logs.takeLast(6).forEach { log ->
                            Text(
                                text = log,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFFE2E8F0),
                                lineHeight = 14.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (!isError) {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet)
                ) {
                    Text(if (isBn) "বন্ধ করুন" else "Close")
                }
            }
        }
    )
}
