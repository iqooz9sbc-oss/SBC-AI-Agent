package com.example.ui.components

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Timeline
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.domain.model.AutonomousTask
import com.example.domain.model.ExecutionRunRecord
import com.example.domain.model.SelfHealingMetrics
import com.example.domain.model.StepStatus
import com.example.domain.model.TaskStatus
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.EmeraldSuccess
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

enum class VisualizerEngine(val titleEn: String, val titleBn: String, val icon: String) {
    RECHARTS_NATIVE("Recharts Style (Native)", "রিচার্টস স্টাইল (নেটিভ)", "📊"),
    D3_INTERACTIVE_SVG("D3.js SVG Visualizer", "ডি৩.জেএস ইন্টারেক্টিভ এসভিজি", "⚡")
}

@Composable
fun SelfHealingProgressTracker(
    task: AutonomousTask?,
    metrics: SelfHealingMetrics,
    isBn: Boolean = false,
    onTriggerDemoTask: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedEngine by remember { mutableStateOf(VisualizerEngine.RECHARTS_NATIVE) }
    var selectedSegment by remember { mutableIntStateOf(0) } // 0 = all, 1 = success, 2 = recovered, 3 = barriers
    var showRecentHistory by remember { mutableStateOf(false) }

    // Live counts combining historical stats and current active task status
    val activeTaskSuccessful = task?.steps?.count { it.status == StepStatus.COMPLETED } ?: 0
    val activeTaskRecovered = task?.steps?.count { it.status == StepStatus.HEALED } ?: 0
    val activeTaskBarriers = task?.steps?.count { it.status == StepStatus.FAILED } ?: 0
    val activeTaskInFlight = task?.steps?.count { it.status == StepStatus.ACTIVE } ?: 0
    val activeTaskPending = task?.steps?.count { it.status == StepStatus.PENDING } ?: 0

    val totalSuccessful = metrics.successfulExecutions + activeTaskSuccessful
    val totalRecovered = metrics.recoveredErrors + activeTaskRecovered
    val totalBarriers = metrics.unresolvedErrors + activeTaskBarriers
    val totalOperations = (totalSuccessful + totalRecovered + totalBarriers).coerceAtLeast(1)

    val successPercent = (totalSuccessful.toFloat() / totalOperations * 100f)
    val recoveredPercent = (totalRecovered.toFloat() / totalOperations * 100f)
    val barrierPercent = (totalBarriers.toFloat() / totalOperations * 100f)

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, ElectricViolet.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("self_healing_progress_tracker")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row with Title and Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = CyberCyan.copy(alpha = 0.15f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Timeline,
                                contentDescription = null,
                                tint = CyberCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isBn) "স্ব-নিরাময় টাস্ক স্ট্যাটাস ট্র্যাকার" else "Self-Healing Task Status Tracker",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isBn) "সফল এক্সিকিউশন বনাম রিকভার্ড এরর ভিজুয়ালাইজার" else "Successful Executions vs. Recovered Errors",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = EmeraldSuccess.copy(alpha = 0.15f),
                    border = BorderStroke(0.5.dp, EmeraldSuccess.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(EmeraldSuccess)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "100% HEALED",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldSuccess
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Engine Mode Switcher (Recharts Native vs D3.js SVG)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(3.dp)
                ) {
                    VisualizerEngine.entries.forEach { engine ->
                        val isSelected = selectedEngine == engine
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) ElectricViolet else Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedEngine = engine }
                                .testTag("engine_tab_${engine.name}")
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(engine.icon, fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isBn) engine.titleBn else engine.titleEn,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Visualizer Body
            when (selectedEngine) {
                VisualizerEngine.RECHARTS_NATIVE -> {
                    RechartsNativeChart(
                        totalSuccessful = totalSuccessful,
                        totalRecovered = totalRecovered,
                        totalBarriers = totalBarriers,
                        successPercent = successPercent,
                        recoveredPercent = recoveredPercent,
                        barrierPercent = barrierPercent,
                        selectedSegment = selectedSegment,
                        onSelectSegment = { selectedSegment = it },
                        isBn = isBn
                    )
                }

                VisualizerEngine.D3_INTERACTIVE_SVG -> {
                    D3InteractiveSvgChart(
                        totalSuccessful = totalSuccessful,
                        totalRecovered = totalRecovered,
                        totalBarriers = totalBarriers,
                        successPercent = successPercent,
                        recoveredPercent = recoveredPercent,
                        barrierPercent = barrierPercent,
                        isBn = isBn
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Key Telemetry Stat Cards Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricChip(
                    title = if (isBn) "সফল এক্সিকিউশন" else "Successful Executions",
                    count = totalSuccessful,
                    percentage = "${String.format(Locale.US, "%.1f", successPercent)}%",
                    color = EmeraldSuccess,
                    icon = Icons.Default.CheckCircle,
                    isSelected = selectedSegment == 1,
                    onClick = { selectedSegment = if (selectedSegment == 1) 0 else 1 },
                    modifier = Modifier.weight(1f)
                )

                MetricChip(
                    title = if (isBn) "স্ব-নিরাময় উদ্ধার" else "Recovered Errors",
                    count = totalRecovered,
                    percentage = "${String.format(Locale.US, "%.1f", recoveredPercent)}%",
                    color = CyberCyan,
                    icon = Icons.Default.Build,
                    isSelected = selectedSegment == 2,
                    onClick = { selectedSegment = if (selectedSegment == 2) 0 else 2 },
                    modifier = Modifier.weight(1f)
                )

                MetricChip(
                    title = if (isBn) "সক্রিয় বাধা" else "Active Barriers",
                    count = totalBarriers,
                    percentage = "${String.format(Locale.US, "%.1f", barrierPercent)}%",
                    color = if (totalBarriers > 0) Color(0xFFEF4444) else Color(0xFF6B7280),
                    icon = Icons.Default.Warning,
                    isSelected = selectedSegment == 3,
                    onClick = { selectedSegment = if (selectedSegment == 3) 0 else 3 },
                    modifier = Modifier.weight(1f)
                )
            }

            // Interactive Segment Detail Callout
            AnimatedVisibility(visible = selectedSegment != 0) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = when (selectedSegment) {
                        1 -> EmeraldSuccess.copy(alpha = 0.1f)
                        2 -> CyberCyan.copy(alpha = 0.1f)
                        else -> Color(0xFFEF4444).copy(alpha = 0.1f)
                    },
                    border = BorderStroke(
                        1.dp,
                        when (selectedSegment) {
                            1 -> EmeraldSuccess.copy(alpha = 0.3f)
                            2 -> CyberCyan.copy(alpha = 0.3f)
                            else -> Color(0xFFEF4444).copy(alpha = 0.3f)
                        }
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when (selectedSegment) {
                                1 -> Icons.Default.CheckCircle
                                2 -> Icons.Default.AutoAwesome
                                else -> Icons.Default.Warning
                            },
                            contentDescription = null,
                            tint = when (selectedSegment) {
                                1 -> EmeraldSuccess
                                2 -> CyberCyan
                                else -> Color(0xFFEF4444)
                            },
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = when (selectedSegment) {
                                1 -> if (isBn) {
                                    "সফল এক্সিকিউশন: $totalSuccessful টি ধাপ প্রথম প্রচেষ্টাতেই কোনো ত্রুটি ছাড়া সফলভাবে সম্পন্ন হয়েছে।"
                                } else {
                                    "Successful Executions: $totalSuccessful operations executed smoothly on first attempt with 100% data integrity."
                                }
                                2 -> if (isBn) {
                                    "স্ব-নিরাময় উদ্ধার: $totalRecovered টি বাধা (API রেট লিমিট ও স্কিমা ত্রুটি) স্বয়ংক্রিয়ভাবে প্যাচ প্রয়োগ করে সুস্থ করা হয়েছে।"
                                } else {
                                    "Recovered via Self-Healing: $totalRecovered execution barriers (rate guards, schema mismatches) automatically hotfixed without manual rollback."
                                }
                                else -> if (isBn) {
                                    "সক্রিয় বাধা: $totalBarriers টি টাস্ক বসের অনুমোদনের অপেক্ষায় রয়েছে।"
                                } else {
                                    "Active Barriers: $totalBarriers tasks paused at error threshold awaiting Boss confirmation."
                                }
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Live Task Pipeline Progress (if task is active)
            if (task != null) {
                Spacer(modifier = Modifier.height(14.dp))
                LivePipelineProgress(
                    task = task,
                    isBn = isBn
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Historical Runs Toggle & List
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showRecentHistory = !showRecentHistory },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isBn) "সাম্প্রতিক অটোনোমাস টাস্কের ইতিহাস (${metrics.recentRuns.size})" else "Recent Task Execution Runs (${metrics.recentRuns.size})",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }

            AnimatedVisibility(visible = showRecentHistory) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    metrics.recentRuns.forEach { record ->
                        HistoricalRunItem(record = record, isBn = isBn)
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------
// Sub-Component 1: Recharts-Style Native Compose Canvas Visualization
// -----------------------------------------------------------------
@Composable
fun RechartsNativeChart(
    totalSuccessful: Int,
    totalRecovered: Int,
    totalBarriers: Int,
    successPercent: Float,
    recoveredPercent: Float,
    barrierPercent: Float,
    selectedSegment: Int,
    onSelectSegment: (Int) -> Unit,
    isBn: Boolean
) {
    val total = (totalSuccessful + totalRecovered + totalBarriers).coerceAtLeast(1)

    // Animated Sweep Angles
    val animatedSuccessSweep by animateFloatAsState(
        targetValue = (successPercent / 100f) * 360f,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "success_sweep"
    )
    val animatedRecoveredSweep by animateFloatAsState(
        targetValue = (recoveredPercent / 100f) * 360f,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "recovered_sweep"
    )
    val animatedBarrierSweep by animateFloatAsState(
        targetValue = (barrierPercent / 100f) * 360f,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "barrier_sweep"
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Donut Chart Container
        Box(
            modifier = Modifier
                .size(200.dp)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val dx = offset.x - center.x
                            val dy = offset.y - center.y
                            var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                            if (angle < 0) angle += 360f
                            // Normalized to starting angle -90deg
                            val normalized = (angle + 90f) % 360f

                            val sEnd = (successPercent / 100f) * 360f
                            val rEnd = sEnd + (recoveredPercent / 100f) * 360f

                            when {
                                normalized <= sEnd -> onSelectSegment(1)
                                normalized <= rEnd -> onSelectSegment(2)
                                else -> onSelectSegment(3)
                            }
                        }
                    }
            ) {
                val strokeWidth = 24.dp.toPx()
                val radius = (size.minDimension - strokeWidth) / 2f
                val centerOffset = Offset(size.width / 2f, size.height / 2f)
                val chartSize = Size(radius * 2f, radius * 2f)
                val topLeft = Offset(centerOffset.x - radius, centerOffset.y - radius)

                // Background track
                drawArc(
                    color = Color.LightGray.copy(alpha = 0.2f),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = chartSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                var currentStart = -90f

                // Segment 1: Successful Executions (Emerald Green)
                if (animatedSuccessSweep > 0f) {
                    drawArc(
                        brush = Brush.sweepGradient(
                            listOf(Color(0xFF34D399), EmeraldSuccess, Color(0xFF059669)),
                            center = centerOffset
                        ),
                        startAngle = currentStart,
                        sweepAngle = animatedSuccessSweep - 2f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = chartSize,
                        style = Stroke(
                            width = if (selectedSegment == 1) strokeWidth + 6.dp.toPx() else strokeWidth,
                            cap = StrokeCap.Round
                        )
                    )
                    currentStart += animatedSuccessSweep
                }

                // Segment 2: Recovered Errors (Cyber Cyan)
                if (animatedRecoveredSweep > 0f) {
                    drawArc(
                        brush = Brush.sweepGradient(
                            listOf(Color(0xFF38BDF8), CyberCyan, Color(0xFF0284C7)),
                            center = centerOffset
                        ),
                        startAngle = currentStart,
                        sweepAngle = animatedRecoveredSweep - 2f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = chartSize,
                        style = Stroke(
                            width = if (selectedSegment == 2) strokeWidth + 6.dp.toPx() else strokeWidth,
                            cap = StrokeCap.Round
                        )
                    )
                    currentStart += animatedRecoveredSweep
                }

                // Segment 3: Barriers (Red)
                if (animatedBarrierSweep > 0f) {
                    drawArc(
                        color = Color(0xFFEF4444),
                        startAngle = currentStart,
                        sweepAngle = animatedBarrierSweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = chartSize,
                        style = Stroke(
                            width = if (selectedSegment == 3) strokeWidth + 6.dp.toPx() else strokeWidth,
                            cap = StrokeCap.Round
                        )
                    )
                }
            }

            // Donut Center Readout
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "${String.format(Locale.US, "%.1f", successPercent + recoveredPercent)}%",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (isBn) "সফলতা হার" else "Resilience Rate",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$total ops",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = CyberCyan
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Recharts-Style Stacked Progress Bar
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (isBn) "অনুপাত বিন্যাস (Distribution)" else "Execution Distribution",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${totalSuccessful}s : ${totalRecovered}h : ${totalBarriers}b",
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = CyberCyan
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Multi-segment horizontal stacked bar
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
            ) {
                Row(modifier = Modifier.fillMaxSize()) {
                    if (successPercent > 0) {
                        Box(
                            modifier = Modifier
                                .weight(successPercent.coerceAtLeast(1f))
                                .fillMaxSize()
                                .background(EmeraldSuccess)
                                .clickable { onSelectSegment(1) }
                        )
                    }
                    if (recoveredPercent > 0) {
                        Box(
                            modifier = Modifier
                                .weight(recoveredPercent.coerceAtLeast(1f))
                                .fillMaxSize()
                                .background(CyberCyan)
                                .clickable { onSelectSegment(2) }
                        )
                    }
                    if (barrierPercent > 0) {
                        Box(
                            modifier = Modifier
                                .weight(barrierPercent.coerceAtLeast(1f))
                                .fillMaxSize()
                                .background(Color(0xFFEF4444))
                                .clickable { onSelectSegment(3) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Legend Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                LegendItem(
                    label = if (isBn) "সফল (${totalSuccessful})" else "Successful (${totalSuccessful})",
                    color = EmeraldSuccess,
                    isSelected = selectedSegment == 1,
                    onClick = { onSelectSegment(1) }
                )
                LegendItem(
                    label = if (isBn) "রিকভার্ড (${totalRecovered})" else "Recovered (${totalRecovered})",
                    color = CyberCyan,
                    isSelected = selectedSegment == 2,
                    onClick = { onSelectSegment(2) }
                )
                LegendItem(
                    label = if (isBn) "বাধা (${totalBarriers})" else "Barriers (${totalBarriers})",
                    color = Color(0xFFEF4444),
                    isSelected = selectedSegment == 3,
                    onClick = { onSelectSegment(3) }
                )
            }
        }
    }
}

// -----------------------------------------------------------------
// Sub-Component 2: Interactive D3.js SVG Visualizer Engine (WebView)
// -----------------------------------------------------------------
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun D3InteractiveSvgChart(
    totalSuccessful: Int,
    totalRecovered: Int,
    totalBarriers: Int,
    successPercent: Float,
    recoveredPercent: Float,
    barrierPercent: Float,
    isBn: Boolean
) {
    val total = (totalSuccessful + totalRecovered + totalBarriers).coerceAtLeast(1)

    // Generate self-contained, responsive D3 SVG HTML document
    val htmlData = remember(totalSuccessful, totalRecovered, totalBarriers, isBn) {
        val sPercent = String.format(Locale.US, "%.1f", successPercent)
        val rPercent = String.format(Locale.US, "%.1f", recoveredPercent)
        val bPercent = String.format(Locale.US, "%.1f", barrierPercent)

        """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
            <style>
                * { box-sizing: border-box; margin: 0; padding: 0; user-select: none; }
                body {
                    background: transparent;
                    color: #E2E8F0;
                    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
                    display: flex;
                    flex-direction: column;
                    align-items: center;
                    justify-content: center;
                    overflow: hidden;
                    width: 100%;
                    height: 100%;
                }
                .chart-container {
                    position: relative;
                    width: 100%;
                    max-width: 320px;
                    display: flex;
                    flex-direction: column;
                    align-items: center;
                }
                svg {
                    width: 100%;
                    height: auto;
                    filter: drop-shadow(0 4px 12px rgba(0,0,0,0.25));
                }
                .slice {
                    cursor: pointer;
                    transition: transform 0.25s ease, filter 0.25s ease;
                    transform-origin: 160px 110px;
                }
                .slice:hover, .slice:active {
                    filter: brightness(1.2);
                    transform: scale(1.04);
                }
                .center-text-title {
                    font-size: 20px;
                    font-weight: 800;
                    fill: #FFFFFF;
                    text-anchor: middle;
                }
                .center-text-sub {
                    font-size: 11px;
                    font-weight: 600;
                    fill: #06B6D4;
                    text-anchor: middle;
                }
                .tooltip {
                    font-size: 11px;
                    font-weight: 600;
                    background: rgba(15, 23, 42, 0.9);
                    border: 1px solid #7C3AED;
                    border-radius: 8px;
                    padding: 6px 10px;
                    color: #FFFFFF;
                    margin-top: 4px;
                    text-align: center;
                    min-height: 28px;
                    width: 90%;
                }
                .legend {
                    display: flex;
                    justify-content: space-around;
                    width: 100%;
                    margin-top: 8px;
                    font-size: 11px;
                }
                .legend-item {
                    display: flex;
                    align-items: center;
                    gap: 5px;
                }
                .dot {
                    width: 8px;
                    height: 8px;
                    border-radius: 50%;
                }
            </style>
        </head>
        <body>
            <div class="chart-container">
                <svg viewBox="0 0 320 220">
                    <defs>
                        <linearGradient id="gradSuccess" x1="0%" y1="0%" x2="100%" y2="100%">
                            <stop offset="0%" stop-color="#34D399" />
                            <stop offset="100%" stop-color="#059669" />
                        </linearGradient>
                        <linearGradient id="gradRecovered" x1="0%" y1="0%" x2="100%" y2="100%">
                            <stop offset="0%" stop-color="#38BDF8" />
                            <stop offset="100%" stop-color="#0284C7" />
                        </linearGradient>
                        <linearGradient id="gradBarrier" x1="0%" y1="0%" x2="100%" y2="100%">
                            <stop offset="0%" stop-color="#F87171" />
                            <stop offset="100%" stop-color="#DC2626" />
                        </linearGradient>
                        <filter id="glow" x="-20%" y="-20%" width="140%" height="140%">
                            <feGaussianBlur stdDeviation="3" result="blur" />
                            <feComposite in="SourceGraphic" in2="blur" operator="over" />
                        </filter>
                    </defs>

                    <!-- Background Base Track -->
                    <circle cx="160" cy="110" r="75" fill="none" stroke="rgba(255,255,255,0.08)" stroke-width="26" />

                    <!-- D3 Generated SVG Arcs -->
                    <g id="arcs-group"></g>

                    <!-- Center Readout -->
                    <text x="160" y="105" class="center-text-title">${String.format(Locale.US, "%.1f", successPercent + recoveredPercent)}%</text>
                    <text x="160" y="125" class="center-text-sub">${if (isBn) "সেলফ-হিল্ড স্থিতিস্থাপকতা" else "Self-Healed Resilience"}</text>
                </svg>

                <div id="tooltip" class="tooltip">
                    ⚡ Tap/Hover segments to inspect D3 runtime telemetry
                </div>

                <div class="legend">
                    <div class="legend-item" onclick="showTip('success')">
                        <div class="dot" style="background:#10B981;"></div>
                        <span>Success ($totalSuccessful)</span>
                    </div>
                    <div class="legend-item" onclick="showTip('recovered')">
                        <div class="dot" style="background:#06B6D4;"></div>
                        <span>Healed ($totalRecovered)</span>
                    </div>
                    <div class="legend-item" onclick="showTip('barrier')">
                        <div class="dot" style="background:#EF4444;"></div>
                        <span>Barriers ($totalBarriers)</span>
                    </div>
                </div>
            </div>

            <script>
                // D3 Math Arc Computation
                const cx = 160, cy = 110, r = 75, strokeW = 26;
                const total = $total;
                const sVal = $totalSuccessful;
                const rVal = $totalRecovered;
                const bVal = $totalBarriers;

                const dataset = [
                    { id: 'success', label: 'Successful Executions', value: sVal, color: 'url(#gradSuccess)', pct: '$sPercent%' },
                    { id: 'recovered', label: 'Recovered via Self-Healing', value: rVal, color: 'url(#gradRecovered)', pct: '$rPercent%' },
                    { id: 'barrier', label: 'Barriers Intercepted', value: bVal, color: 'url(#gradBarrier)', pct: '$bPercent%' }
                ].filter(d => d.value > 0);

                const group = document.getElementById('arcs-group');
                let currentAngle = -90; // Start at 12 o'clock

                function polarToCartesian(centerX, centerY, radius, angleInDegrees) {
                    const angleInRadians = (angleInDegrees) * Math.PI / 180.0;
                    return {
                        x: centerX + (radius * Math.cos(angleInRadians)),
                        y: centerY + (radius * Math.sin(angleInRadians))
                    };
                }

                function describeArc(x, y, radius, startAngle, endAngle) {
                    const start = polarToCartesian(x, y, radius, endAngle);
                    const end = polarToCartesian(x, y, radius, startAngle);
                    const largeArcFlag = endAngle - startAngle <= 180 ? "0" : "1";
                    return [
                        "M", start.x, start.y,
                        "A", radius, radius, 0, largeArcFlag, 0, end.x, end.y
                    ].join(" ");
                }

                dataset.forEach(item => {
                    const sweep = (item.value / total) * 360;
                    const path = document.createElementNS("http://www.w3.org/2000/svg", "path");
                    const start = currentAngle;
                    const end = currentAngle + sweep - 1.5;

                    path.setAttribute("d", describeArc(cx, cy, r, start, end));
                    path.setAttribute("fill", "none");
                    path.setAttribute("stroke", item.color);
                    path.setAttribute("stroke-width", strokeW);
                    path.setAttribute("stroke-linecap", "round");
                    path.setAttribute("class", "slice");

                    path.onclick = () => showTip(item.id);
                    path.onmouseover = () => showTip(item.id);

                    group.appendChild(path);
                    currentAngle += sweep;
                });

                function showTip(type) {
                    const tip = document.getElementById('tooltip');
                    if (type === 'success') {
                        tip.innerHTML = '🟢 <b>$totalSuccessful Successful ($sPercent%)</b> — Executed cleanly without interruption.';
                    } else if (type === 'recovered') {
                        tip.innerHTML = '🔵 <b>$totalRecovered Recovered ($rPercent%)</b> — Automated self-healing hotfix applied.';
                    } else {
                        tip.innerHTML = '🔴 <b>$totalBarriers Barriers ($bPercent%)</b> — Diagnostic triggered, awaiting action.';
                    }
                }
            </script>
        </body>
        </html>
        """.trimIndent()
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF0F172A))
            .testTag("d3_svg_webview_container"),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    setBackgroundColor(0) // Transparent
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.useWideViewPort = true
                    settings.loadWithOverviewMode = true
                    webViewClient = WebViewClient()
                    loadDataWithBaseURL(null, htmlData, "text/html", "UTF-8", null)
                }
            },
            update = { webView ->
                webView.loadDataWithBaseURL(null, htmlData, "text/html", "UTF-8", null)
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}

// -----------------------------------------------------------------
// Sub-Component 3: Live Pipeline Progress Tracking
// -----------------------------------------------------------------
@Composable
fun LivePipelineProgress(
    task: AutonomousTask,
    isBn: Boolean
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isBn) "লাইভ পাইপলাইন অগ্রগতি" else "Live Pipeline Progress",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = CyberCyan
                )

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = when (task.status) {
                        TaskStatus.ERROR_DETECTED -> Color(0xFFEF4444).copy(alpha = 0.2f)
                        TaskStatus.HEALING -> CyberCyan.copy(alpha = 0.2f)
                        TaskStatus.COMPLETED -> EmeraldSuccess.copy(alpha = 0.2f)
                        else -> ElectricViolet.copy(alpha = 0.2f)
                    }
                ) {
                    Text(
                        text = task.status.name,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (task.status) {
                            TaskStatus.ERROR_DETECTED -> Color(0xFFEF4444)
                            TaskStatus.HEALING -> CyberCyan
                            TaskStatus.COMPLETED -> EmeraldSuccess
                            else -> ElectricViolet
                        },
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Step Node Tracker Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                task.steps.forEachIndexed { idx, step ->
                    val isCompleted = step.status == StepStatus.COMPLETED
                    val isHealed = step.status == StepStatus.HEALED
                    val isFailed = step.status == StepStatus.FAILED
                    val isActive = step.status == StepStatus.ACTIVE

                    // Node Circle
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isCompleted -> EmeraldSuccess
                                    isHealed -> CyberCyan
                                    isFailed -> Color(0xFFEF4444)
                                    isActive -> ElectricViolet
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isActive) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                        } else {
                            Text(
                                text = when {
                                    isCompleted -> "✓"
                                    isHealed -> "★"
                                    isFailed -> "!"
                                    else -> "${idx + 1}"
                                },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    // Connector line between nodes
                    if (idx < task.steps.size - 1) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(3.dp)
                                .background(
                                    if (idx < task.currentStepIndex) {
                                        if (isHealed) CyberCyan else EmeraldSuccess
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                                    }
                                )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Current Step Summary
            val currentStep = task.steps.getOrNull(task.currentStepIndex.coerceAtMost(task.steps.size - 1))
            if (currentStep != null) {
                Text(
                    text = "Current: " + (if (isBn) currentStep.titleBn else currentStep.titleEn),
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// -----------------------------------------------------------------
// Sub-Component 4: Historical Run Item
// -----------------------------------------------------------------
@Composable
fun HistoricalRunItem(
    record: ExecutionRunRecord,
    isBn: Boolean
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isBn) record.taskNameBn else record.taskNameEn,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${record.durationSeconds}s duration • ${record.totalSteps} steps",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Stacked mini pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = EmeraldSuccess.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "${record.successfulSteps} OK",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldSuccess,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                if (record.recoveredSteps > 0) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = CyberCyan.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "${record.recoveredSteps} HEALED",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberCyan,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------
// Sub-Component 5: Helper Metric Chip
// -----------------------------------------------------------------
@Composable
fun MetricChip(
    title: String,
    count: Int,
    percentage: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) color.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(
            1.dp,
            if (isSelected) color else color.copy(alpha = 0.25f)
        ),
        modifier = modifier.clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = percentage,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$count",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = title,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

@Composable
fun LegendItem(
    label: String,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .clickable { onClick() }
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
