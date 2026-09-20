package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.AutomationRule
import com.example.domain.model.AutonomousTask
import com.example.domain.model.BusinessStrategy
import com.example.domain.model.DailyCompanyReport
import com.example.domain.model.FinancialTransaction
import com.example.domain.model.FinancialType
import com.example.domain.model.MarketingWorkflow
import com.example.domain.model.SelfHealingMetrics
import com.example.domain.model.StepStatus
import com.example.domain.model.TaskStatus
import com.example.ui.components.SelfHealingProgressTracker
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.viewmodel.AutomationSubTab
import com.example.ui.viewmodel.AutomationViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AutomationScreen(
    viewModel: AutomationViewModel,
    isBn: Boolean = false,
    modifier: Modifier = Modifier
) {
    val selectedSubTab by viewModel.selectedSubTab.collectAsStateWithLifecycle()
    val dailyReport by viewModel.dailyReport.collectAsStateWithLifecycle()
    val strategies by viewModel.strategies.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val marketingWorkflows by viewModel.marketingWorkflows.collectAsStateWithLifecycle()
    val currentTask by viewModel.currentTask.collectAsStateWithLifecycle()
    val selfHealingMetrics by viewModel.selfHealingMetrics.collectAsStateWithLifecycle()
    val rules by viewModel.rules.collectAsStateWithLifecycle()
    val executionMessage by viewModel.executionMessage.collectAsStateWithLifecycle()

    var showAddRuleDialog by remember { mutableStateOf(false) }
    var showAddTxDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("automation_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Sub-navigation Tabs (Business & Workflows | Autonomous Tasks | Device Routines)
            ScrollableTabRow(
                selectedTabIndex = selectedSubTab.ordinal,
                edgePadding = 12.dp,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = ElectricViolet,
                modifier = Modifier.fillMaxWidth()
            ) {
                AutomationSubTab.entries.forEach { tab ->
                    Tab(
                        selected = selectedSubTab == tab,
                        onClick = { viewModel.selectSubTab(tab) },
                        text = {
                            Text(
                                text = if (isBn) tab.titleBn else tab.titleEn,
                                fontWeight = if (selectedSubTab == tab) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        },
                        modifier = Modifier.testTag("subtab_${tab.name}")
                    )
                }
            }

            // Execution Banner
            AnimatedVisibility(
                visible = executionMessage != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Surface(
                    color = EmeraldSuccess.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = executionMessage ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = EmeraldSuccess,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { viewModel.clearExecutionMessage() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = EmeraldSuccess, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // SubTab Content
            when (selectedSubTab) {
                AutomationSubTab.BUSINESS -> {
                    BusinessManagerView(
                        report = dailyReport,
                        transactions = transactions,
                        strategies = strategies,
                        workflows = marketingWorkflows,
                        isBn = isBn,
                        onRefreshReport = { viewModel.refreshDailyReport() },
                        onAddTxClick = { showAddTxDialog = true },
                        onExecuteStrategy = { viewModel.executeStrategy(it) },
                        onLaunchMarketing = { viewModel.launchMarketingWorkflow(it) }
                    )
                }

                AutomationSubTab.SELF_HEALING -> {
                    SelfHealingTaskView(
                        task = currentTask,
                        metrics = selfHealingMetrics,
                        isBn = isBn,
                        onStartDemo = {
                            viewModel.startCustomAutonomousTask("Autonomous Market Penetration & Self-Healing", true)
                        },
                        onConfirmFix = { viewModel.confirmSelfHealingFix() },
                        onCancelTask = { viewModel.cancelAutonomousTask() },
                        onDismiss = { viewModel.dismissAutonomousTask() }
                    )
                }

                AutomationSubTab.DEVICE_RULES -> {
                    DeviceRoutinesView(
                        rules = rules,
                        isBn = isBn,
                        onToggle = { viewModel.toggleRule(it) },
                        onExecute = { viewModel.executeRule(it) },
                        onDelete = { viewModel.deleteRule(it.id) }
                    )
                }
            }
        }

        // Floating Action Button
        if (selectedSubTab == AutomationSubTab.DEVICE_RULES) {
            FloatingActionButton(
                onClick = { showAddRuleDialog = true },
                containerColor = ElectricViolet,
                contentColor = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
                    .testTag("add_automation_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Routine")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isBn) "নতুন রুটিন" else "Add Routine", fontWeight = FontWeight.SemiBold)
                }
            }
        } else if (selectedSubTab == AutomationSubTab.BUSINESS) {
            FloatingActionButton(
                onClick = { showAddTxDialog = true },
                containerColor = EmeraldSuccess,
                contentColor = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
                    .testTag("add_transaction_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Transaction")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isBn) "হিসাব যোগ" else "Add Record", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }

    // Dialogs
    if (showAddRuleDialog) {
        AddAutomationDialog(
            isBn = isBn,
            onDismiss = { showAddRuleDialog = false },
            onConfirm = { title, titleBn, desc, descBn, trigger, action ->
                viewModel.addRule(title, titleBn, desc, descBn, trigger, action)
                showAddRuleDialog = false
            }
        )
    }

    if (showAddTxDialog) {
        AddFinancialDialog(
            isBn = isBn,
            onDismiss = { showAddTxDialog = false },
            onConfirm = { title, amount, type, category ->
                viewModel.addFinancialTransaction(title, amount, type, category)
                showAddTxDialog = false
            }
        )
    }
}

// -------------------------------------------------------------
// Sub-view 1: Business & Workflow Manager
// -------------------------------------------------------------
@Composable
fun BusinessManagerView(
    report: DailyCompanyReport,
    transactions: List<FinancialTransaction>,
    strategies: List<BusinessStrategy>,
    workflows: List<MarketingWorkflow>,
    isBn: Boolean,
    onRefreshReport: () -> Unit,
    onAddTxClick: () -> Unit,
    onExecuteStrategy: (BusinessStrategy) -> Unit,
    onLaunchMarketing: (MarketingWorkflow) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Section: Daily Company Executive Report Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, ElectricViolet.copy(alpha = 0.3f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("📊", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (isBn) "দৈনিক কোম্পানি প্রতিবেদন" else "Daily Company Report",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = report.dateString,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        IconButton(onClick = onRefreshReport, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = CyberCyan)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Briefing Text
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isBn) report.executiveBriefingBn else report.executiveBriefingEn,
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 22.sp,
                            modifier = Modifier.padding(12.dp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // KPIs Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        report.kpis.take(2).forEach { kpi ->
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(kpi.label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(kpi.value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = EmeraldSuccess)
                                    Text(kpi.change, fontSize = 10.sp, color = CyberCyan)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        report.kpis.drop(2).take(2).forEach { kpi ->
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(kpi.label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(kpi.value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = ElectricViolet)
                                    Text(kpi.change, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section: Strategic Suggestions & Boss Confirmation
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("👑", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isBn) "কৌশলগত পরামর্শ ও বাস্তবায়ন" else "Strategic Suggestions & Execution",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = CyberCyan
                    )
                }
            }
        }

        items(strategies, key = { it.id }) { strategy ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                border = if (strategy.isExecuted) BorderStroke(1.dp, EmeraldSuccess) else null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isBn) strategy.titleBn else strategy.titleEn,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        Surface(
                            color = ElectricViolet.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = strategy.category,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = ElectricViolet,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (isBn) strategy.summaryBn else strategy.summaryEn,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Action Steps Summary
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = if (isBn) "বাস্তবায়ন পদক্ষেপ:" else "Execution Roadmap:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        strategy.actionSteps.take(3).forEach { step ->
                            Text(
                                text = "• $step",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = strategy.estimatedImpact,
                            fontSize = 10.sp,
                            color = EmeraldSuccess,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )

                        if (strategy.isExecuted) {
                            Surface(
                                color = EmeraldSuccess.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (isBn) "বাস্তবায়িত" else "Executed", fontSize = 11.sp, color = EmeraldSuccess, fontWeight = FontWeight.Bold)
                                }
                            }
                        } else {
                            Button(
                                onClick = { onExecuteStrategy(strategy) },
                                colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text(
                                    text = if (isBn) "বস, বাস্তবায়ন করুন" else "Boss, Execute Strategy",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section: Free Zero-Budget Marketing Workflows
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("📢", fontSize = 18.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isBn) "জিরো-বাজেট ফ্রি মার্কেটিং ওয়ার্কফ্লো" else "Zero-Budget Marketing Workflows",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = CyberCyan
                )
            }
        }

        items(workflows, key = { it.id }) { wf ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isBn) wf.titleBn else wf.titleEn,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            color = EmeraldSuccess.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "ZERO BUDGET",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldSuccess,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Channel: ${wf.channel} • Schedule: ${wf.executionSchedule}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isBn) wf.hookBn else wf.hookEn,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(8.dp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = { onLaunchMarketing(wf) },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(Icons.Default.Campaign, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isBn) "ক্যাম্পেইন শুরু করুন" else "Launch Workflow", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Section: Financial Accounting Transactions
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("💰", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isBn) "আর্থিক হিসাব নিকাশ লেজার" else "Financial Accounting Ledger",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = CyberCyan
                    )
                }

                TextButton(onClick = onAddTxClick) {
                    Text(if (isBn) "+ হিসাব যোগ" else "+ Add Entry", fontSize = 12.sp, color = ElectricViolet)
                }
            }
        }

        items(transactions, key = { it.id }) { tx ->
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(tx.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Text("${tx.category} • " + SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault()).format(Date(tx.timestamp)), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Text(
                        text = (if (tx.type == FinancialType.INCOME) "+$" else "-$") + String.format(Locale.US, "%.2f", tx.amount),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (tx.type == FinancialType.INCOME) EmeraldSuccess else Color(0xFFEF4444)
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Sub-view 2: Autonomous Tasks & Self-Healing
// -------------------------------------------------------------
@Composable
fun SelfHealingTaskView(
    task: AutonomousTask?,
    metrics: SelfHealingMetrics,
    isBn: Boolean,
    onStartDemo: () -> Unit,
    onConfirmFix: () -> Unit,
    onCancelTask: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Visual Progress Tracking Component (Recharts / D3)
        SelfHealingProgressTracker(
            task = task,
            metrics = metrics,
            isBn = isBn,
            onTriggerDemoTask = onStartDemo
        )

        if (task == null) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = CircleShape,
                        color = ElectricViolet.copy(alpha = 0.15f),
                        modifier = Modifier.size(64.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Psychology, contentDescription = null, tint = ElectricViolet, modifier = Modifier.size(32.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = if (isBn) "স্ব-নিরাময় অটোনোমাস টাস্ক ইঞ্জিন" else "Autonomous Self-Healing Task Engine",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (isBn) {
                            "টাস্ক চলতে চলতে কোনো ত্রুটি দেখা দিলে এজেন্ট নিজে থেকে সমাধান শনাক্ত করে বসের অনুমোদন নিয়ে কাজটি সম্পন্ন করে।"
                        } else {
                            "Continues execution across multi-step objectives. When execution barriers occur, the AI diagnoses the root-cause and asks Boss to approve the self-healing patch."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onStartDemo,
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isBn) "স্ব-নিরাময় টাস্ক চালু করুন" else "Launch Self-Healing Workflow")
                    }
                }
            }
        } else {
            // Live Task Monitor Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.5.dp, if (task.status == TaskStatus.ERROR_DETECTED) Color(0xFFEF4444) else ElectricViolet),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isBn) task.titleBn else task.titleEn,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Status: ${task.status.name} • Step ${task.currentStepIndex + 1}/${task.steps.size}",
                                style = MaterialTheme.typography.labelSmall,
                                color = CyberCyan
                            )
                        }

                        if (task.status == TaskStatus.RUNNING || task.status == TaskStatus.HEALING) {
                            CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp, color = ElectricViolet)
                        } else if (task.status == TaskStatus.COMPLETED) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(24.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Steps Progress
                    task.steps.forEachIndexed { idx, step ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            when (step.status) {
                                StepStatus.COMPLETED -> Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(16.dp))
                                StepStatus.HEALED -> Icon(Icons.Default.Security, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(16.dp))
                                StepStatus.FAILED -> Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                                StepStatus.ACTIVE -> CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = ElectricViolet)
                                StepStatus.PENDING -> Box(modifier = Modifier.size(14.dp).background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f), CircleShape))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${idx + 1}. " + (if (isBn) step.titleBn else step.titleEn),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Self-Healing Critical Action Box
                    if (task.status == TaskStatus.ERROR_DETECTED && task.requiresBossConfirmation) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                            border = BorderStroke(1.dp, Color(0xFFEF4444))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "⚠️ " + (if (isBn) "ত্রুটি শনাক্ত: " else "Execution Barrier: ") + (task.errorDetails ?: ""),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF991B1B)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "🛠️ " + (if (isBn) task.suggestedSolutionBn else task.suggestedSolutionEn),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF1E293B)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (isBn) task.bossQuestionBn else task.bossQuestionEn,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = onConfirmFix,
                                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(if (isBn) "সমাধান প্রয়োগ করুন" else "Apply Fix & Continue", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    }
                                    OutlinedButton(
                                        onClick = onCancelTask,
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(if (isBn) "বাতিল" else "Cancel", color = Color(0xFFDC2626), fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }

                    // Completed Banner
                    if (task.status == TaskStatus.COMPLETED) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (isBn) task.resultSummaryBn ?: "" else task.resultSummaryEn ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = EmeraldSuccess,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(if (isBn) "সম্পন্ন" else "Dismiss Task")
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Sub-view 3: Device Routines View
// -------------------------------------------------------------
@Composable
fun DeviceRoutinesView(
    rules: List<AutomationRule>,
    isBn: Boolean,
    onToggle: (AutomationRule) -> Unit,
    onExecute: (AutomationRule) -> Unit,
    onDelete: (AutomationRule) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(rules, key = { it.id }) { rule ->
            AutomationRuleCard(
                rule = rule,
                isBn = isBn,
                onToggle = { onToggle(rule) },
                onExecute = { onExecute(rule) },
                onDelete = { onDelete(rule) }
            )
        }
    }
}

@Composable
fun AutomationRuleCard(
    rule: AutomationRule,
    isBn: Boolean,
    onToggle: () -> Unit,
    onExecute: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("automation_rule_${rule.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = null,
                        tint = if (rule.isEnabled) CyberCyan else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isBn) rule.titleBn else rule.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Switch(
                    checked = rule.isEnabled,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = ElectricViolet
                    ),
                    modifier = Modifier.testTag("toggle_rule_${rule.id}")
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (isBn) rule.descriptionBn else rule.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = (if (isBn) "⏰ ট্রিগার: " else "⏰ Trigger: ") + (if (isBn) rule.triggerTextBn else rule.triggerText),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = (if (isBn) "⚡ অ্যাকশন: " else "⚡ Action: ") + (if (isBn) rule.actionTextBn else rule.actionText),
                        style = MaterialTheme.typography.labelSmall,
                        color = CyberCyan,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val execText = if (rule.lastExecutedTime != null) {
                    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
                    (if (isBn) "সর্বশেষ: " else "Last: ") + sdf.format(Date(rule.lastExecutedTime))
                } else {
                    if (isBn) "চালানো হয়নি" else "Never triggered"
                }

                Text(
                    text = "$execText • (${rule.executionCount}x)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(
                        onClick = onExecute,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier
                            .height(32.dp)
                            .testTag("run_rule_${rule.id}")
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isBn) "টেস্ট করুন" else "Run Now", fontSize = 11.sp)
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(32.dp)
                            .padding(start = 4.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete routine",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddAutomationDialog(
    isBn: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (title: String, titleBn: String, desc: String, descBn: String, trigger: String, action: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var trigger by remember { mutableStateOf("") }
    var action by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isBn) "নতুন অটোমেশন রুটিন যোগ করুন" else "Create New Automation Routine",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(if (isBn) "রুটিনের নাম" else "Routine Name") },
                    placeholder = { Text(if (isBn) "যেমন: ওয়ার্ক ফোকাস মোড" else "e.g., Work Focus Routine") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(if (isBn) "বিবরণ" else "Description") },
                    placeholder = { Text(if (isBn) "কাজের সময় নোটিফিকেশন নিয়ন্ত্রণ" else "Mutes distractions during work") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = trigger,
                    onValueChange = { trigger = it },
                    label = { Text(if (isBn) "ট্রিগার শর্ত" else "Trigger Condition") },
                    placeholder = { Text(if (isBn) "যেমন: সকাল ৯:০০ টায়" else "e.g., At 9:00 AM on Weekdays") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = action,
                    onValueChange = { action = it },
                    label = { Text(if (isBn) "অ্যাকশন ফলাফল" else "Action Execution") },
                    placeholder = { Text(if (isBn) "ফোকাস মোড অন করো" else "e.g., Set Do Not Disturb & Eco Mode") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && action.isNotBlank()) {
                        onConfirm(title, title, description, description, trigger, action)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet)
            ) {
                Text(if (isBn) "রুটিন তৈরি করুন" else "Create Routine")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isBn) "বাতিল" else "Cancel")
            }
        }
    )
}

@Composable
fun AddFinancialDialog(
    isBn: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (title: String, amount: Double, type: FinancialType, category: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }
    var isIncome by remember { mutableStateOf(true) }
    var category by remember { mutableStateOf("Sales") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (isBn) "নতুন আর্থিক লেনদেন যোগ করুন" else "Record Financial Transaction", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { isIncome = true },
                        colors = ButtonDefaults.buttonColors(containerColor = if (isIncome) EmeraldSuccess else MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (isBn) "আয় (Income)" else "Income", color = if (isIncome) Color.White else MaterialTheme.colorScheme.onSurface)
                    }
                    Button(
                        onClick = { isIncome = false },
                        colors = ButtonDefaults.buttonColors(containerColor = if (!isIncome) Color(0xFFEF4444) else MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (isBn) "ব্যয় (Expense)" else "Expense", color = if (!isIncome) Color.White else MaterialTheme.colorScheme.onSurface)
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(if (isBn) "বিবরণ" else "Description") },
                    placeholder = { Text("e.g. Client SaaS Subscription") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text(if (isBn) "পরিমাণ ($)" else "Amount ($)") },
                    placeholder = { Text("e.g. 250.00") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text(if (isBn) "ক্যাটাগরি" else "Category") },
                    placeholder = { Text("Software, Cloud, Marketing...") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountStr.toDoubleOrNull() ?: 0.0
                    if (title.isNotBlank() && amt > 0.0) {
                        onConfirm(title, amt, if (isIncome) FinancialType.INCOME else FinancialType.EXPENSE, category)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet)
            ) {
                Text(if (isBn) "সংরক্ষণ করুন" else "Save Entry")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isBn) "বাতিল" else "Cancel")
            }
        }
    )
}
