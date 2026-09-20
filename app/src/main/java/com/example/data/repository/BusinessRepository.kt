package com.example.data.repository

import com.example.domain.model.BusinessKpi
import com.example.domain.model.BusinessStrategy
import com.example.domain.model.DailyCompanyReport
import com.example.domain.model.FinancialTransaction
import com.example.domain.model.FinancialType
import com.example.domain.model.MarketingWorkflow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BusinessRepository {

    private val _transactions = MutableStateFlow<List<FinancialTransaction>>(
        listOf(
            FinancialTransaction(
                id = 1,
                title = "Enterprise SaaS Subscription (Client Alpha)",
                amount = 1250.00,
                type = FinancialType.INCOME,
                category = "Software Sales",
                timestamp = System.currentTimeMillis() - 86400000L
            ),
            FinancialTransaction(
                id = 2,
                title = "Cloud Infrastructure & GPUs (Google Cloud)",
                amount = 210.50,
                type = FinancialType.EXPENSE,
                category = "Infrastructure",
                timestamp = System.currentTimeMillis() - 72000000L
            ),
            FinancialTransaction(
                id = 3,
                title = "AI Model API Billing (Gemini Flash)",
                amount = 45.00,
                type = FinancialType.EXPENSE,
                category = "AI Operations",
                timestamp = System.currentTimeMillis() - 48000000L
            ),
            FinancialTransaction(
                id = 4,
                title = "Mobile In-App Credit Package Purchases",
                amount = 380.00,
                type = FinancialType.INCOME,
                category = "Freemium Sales",
                timestamp = System.currentTimeMillis() - 14400000L
            ),
            FinancialTransaction(
                id = 5,
                title = "Domain & SSL Security Certification",
                amount = 25.00,
                type = FinancialType.EXPENSE,
                category = "Security",
                timestamp = System.currentTimeMillis() - 3600000L
            )
        )
    )
    val transactions: StateFlow<List<FinancialTransaction>> = _transactions.asStateFlow()

    private val _strategies = MutableStateFlow<List<BusinessStrategy>>(
        listOf(
            BusinessStrategy(
                id = "strat_freemium_growth",
                titleEn = "Freemium Viral Referral & Credit Loop",
                titleBn = "ফ্রিমিয়াম ভাইরাল রেফারেল ও ক্রেডিট লুপ",
                titleBanglish = "Freemium Viral Referral & Credit Loop",
                category = "Growth Engine",
                summaryEn = "Incentivize existing active users with 100 free AI credits for every referred team member who creates an account. This reduces customer acquisition cost (CAC) by 64% while organically accelerating daily active usage.",
                summaryBn = "সক্রিয় ব্যবহারকারীদের প্রতিটি রেফারেলের জন্য ১০০টি ফ্রি এআই ক্রেডিট প্রদান করা হবে। এতে কাস্টমার অ্যাকুইজিশন খরচ ৬৪% কমে যাবে এবং দৈনিক ব্যবহারকারীর সংখ্যা দ্রুত বৃদ্ধি পাবে।",
                summaryBanglish = "Active user-der proti refer er jonno 100 free AI credit deya hobe. Ete CAC 64% kome jabe ebong daily active users druto barbe.",
                actionSteps = listOf(
                    "Deploy automated dynamic invite link generator in Chat & Settings",
                    "Configure credit attribution ledger on referee account activation",
                    "Trigger real-time push congratulation on milestone completions",
                    "Measure retention cohorts at Day 7, Day 14, and Day 30"
                ),
                estimatedImpact = "+42% Monthly Active Users • CAC reduced to near zero"
            ),
            BusinessStrategy(
                id = "strat_b2b_bangladesh",
                titleEn = "B2B Enterprise Automation Suite for SMEs",
                titleBn = "ক্ষুদ্র ও মাঝারি শিল্পের জন্য বি২বি অটোমেশন স্যুট",
                titleBanglish = "SME-der jonno B2B Enterprise Automation Suite",
                category = "Market Expansion",
                summaryEn = "Package autonomous accounting, client customer support, and local invoicing as a turnkey white-label assistant for South Asian retail and tech startups with local bKash/Nagad and USD card settlements.",
                summaryBn = "স্থানীয় খুচরা ও প্রযুক্তি স্টার্টআপদের জন্য অটোনোমাস অ্যাকাউন্টিং, কাস্টমার সাপোর্ট ও ইনভয়েসিং সমাধান একটি সম্পূর্ণ হোয়াইট-লেবেল সহকারী হিসেবে প্যাকেজ করুন।",
                summaryBanglish = "Local retail ebong tech startup-der jonno autonomous accounting, customer support o invoicing solution white-label assistant hishebe launch korun.",
                actionSteps = listOf(
                    "Bundle Multi-lingual Banglish + Bangla OCR receipt parsing engine",
                    "Publish 1-click accounting export to standard spreadsheet formats",
                    "Establish B2B corporate tier subscription billing with automated invoicing",
                    "Initiate direct founder LinkedIn & WhatsApp outreach cadence"
                ),
                estimatedImpact = "+$4,800 MRR potential within 90 days"
            ),
            BusinessStrategy(
                id = "strat_cost_reduction",
                titleEn = "Smart Token Routing & Self-Healing Infrastructure",
                titleBn = "স্মার্ট টোকেন রাউটিং ও স্ব-নিরাময় পরিকাঠামো",
                titleBanglish = "Smart Token Routing & Self-Healing Infra",
                category = "Margin Expansion",
                summaryEn = "Route lightweight conversational queries to on-device/cached engines while reserving Gemini Flash cloud calls for complex reasoning. Reduces cloud API expenses by 58% while maintaining sub-second response latency.",
                summaryBn = "সহজ সাধারণ প্রশ্নগুলো অন-ডিভাইস লোকাল ইঞ্জিনে প্রক্রিয়া করুন এবং জটিল বিশ্লেষণের জন্য জেমিনাই ক্লাউড ব্যবহার করুন। এতে খরচে ৫৮% সাশ্রয় হবে।",
                summaryBanglish = "Simple query gulo local engine-e process korun ebong deep analysis-er jonno Gemini use korun. Ete 58% cost save hobe.",
                actionSteps = listOf(
                    "Implement heuristic query complexity classifier in AIAgentEngine",
                    "Cache repetitive business KPIs and frequent memory retrievals",
                    "Enable dynamic rate-limit throttle protection with auto-recovery",
                    "Track cost per query in the daily financial ledger"
                ),
                estimatedImpact = "Gross margin expansion from 62% to 84%"
            )
        )
    )
    val strategies: StateFlow<List<BusinessStrategy>> = _strategies.asStateFlow()

    private val _marketingWorkflows = MutableStateFlow<List<MarketingWorkflow>>(
        listOf(
            MarketingWorkflow(
                id = "mkt_linkedin_x",
                channel = "LinkedIn & X (Twitter)",
                titleEn = "Autonomous Thought Leadership Engine",
                titleBn = "স্বয়ংক্রিয় থট লিডারশিপ ইঞ্জিন",
                titleBanglish = "Autonomous Thought Leadership Engine",
                hookEn = "\"How an Autonomous AI Agent ran our daily accounting and saved $1,200/month...\"",
                hookBn = "\"কীভাবে একটি অটোনোমাস এআই এজেন্ট আমাদের দৈনিক হিসাব-নিকাশ পরিচালনা করে মাসে ১,২০০ ডলার বাঁচালো...\"",
                hookBanglish = "\"Kivabe ekta Autonomous AI Agent amader daily accounting handle kore mashe $1,200 save korlo...\"",
                stepDetails = listOf(
                    "Analyze daily business performance data to identify high-impact statistics",
                    "Draft structured carousel outline highlighting problem, AI solution, and actionable steps",
                    "Include engagement question targeting founders, developers, and agency owners",
                    "Queue cross-posting during peak engagement window (9:00 AM - 11:30 AM)"
                ),
                isZeroBudget = true,
                executionSchedule = "Daily 9:30 AM"
            ),
            MarketingWorkflow(
                id = "mkt_reddit_community",
                channel = "Reddit & Developer Communities",
                titleEn = "Organic Value-First Problem-Solver Funnel",
                titleBn = "কমিউনিটি অর্গানিক ভ্যালু শেয়ারিং ফানেল",
                titleBanglish = "Community Organic Value Sharing Funnel",
                hookEn = "\"We open-sourced an autonomous multi-step task engine in Jetpack Compose + Gemini. Here is what we learned.\"",
                hookBn = "\"আমরা জেটপ্যাক কম্পোজ ও জেমিনাই দিয়ে তৈরি একটি অটোনোমাস মাল্টি-স্টেপ টাস্ক ইঞ্জিন উন্মুক্ত করেছি। এখানে আমাদের অভিজ্ঞতা...\"",
                hookBanglish = "\"Amra Jetpack Compose o Gemini diye toiri autonomous multi-step task engine open-source korechi. Ekhane amader shob experience...\"",
                stepDetails = listOf(
                    "Identify top 5 trending discussions in r/AndroidDev, r/SaaS, and tech forums",
                    "Formulate technical, helpful, non-promotional responses providing real architectural code",
                    "Provide link to documentation with transparent architectural diagrams",
                    "Engage and respond to top 10 comment threads with thoughtful follow-ups"
                ),
                isZeroBudget = true,
                executionSchedule = "Every Tuesday & Thursday"
            ),
            MarketingWorkflow(
                id = "mkt_short_video",
                channel = "YouTube Shorts & TikTok",
                titleEn = "30-Second Micro-Case Study Video Pipeline",
                titleBn = "৩০ সেকেন্ডের মাইক্রো-কেস স্টাডি ভিডিও পাইপলাইন",
                titleBanglish = "30-Second Micro Case Study Video Pipeline",
                hookEn = "\"Watch this AI agent diagnose its own network error and heal itself in 3 seconds...\"",
                hookBn = "\"দেখুন কীভাবে এই এআই এজেন্ট নিজেই নেটওয়ার্ক ত্রুটি শনাক্ত করে ৩ সেকেন্ডে নিজেকে নিরাময় করল...\"",
                hookBanglish = "\"Dekhun kivabe ei AI agent nijer network error dhore 3 second-e heal kore fellam...\"",
                stepDetails = listOf(
                    "Extract 15-second screen recording demonstrating real-time self-healing UI",
                    "Generate bilingual caption overlays (English & Bangla subtitle banners)",
                    "Pair with high-retention trending audio hook and clear visual call-to-action",
                    "Publish directly across YouTube Shorts, Instagram Reels, and TikTok"
                ),
                isZeroBudget = true,
                executionSchedule = "3x Weekly at 6:00 PM"
            ),
            MarketingWorkflow(
                id = "mkt_cold_email",
                channel = "Programmatic Outreach",
                titleEn = "Hyper-Personalized Startup Founder Playbook",
                titleBn = "স্টার্টআপ প্রতিষ্ঠাতাদের জন্য পার্সোনালাইজড প্লেবুক",
                titleBanglish = "Startup Founder Hyper-Personalized Playbook",
                hookEn = "\"Automating internal repetitive workflows for fast-growing development teams\"",
                hookBn = "\"দ্রুত বর্ধনশীল প্রযুক্তি টিমের জন্য অভ্যন্তরীণ পুনরাবৃত্তিমূলক কাজের অটোমেশন\"",
                hookBanglish = "\"Fast growing tech team-er jonno internal repetitive kajer automation\"",
                stepDetails = listOf(
                    "Scrape verified founder emails from publicly accessible startup directories",
                    "Craft 3-sentence personalized observation citing recent product launches",
                    "Offer 100 free pilot credits with zero obligation demo link",
                    "Automate gentle 4-day reminder with case study metric attachment"
                ),
                isZeroBudget = true,
                executionSchedule = "Monday Mornings 8:00 AM"
            )
        )
    )
    val marketingWorkflows: StateFlow<List<MarketingWorkflow>> = _marketingWorkflows.asStateFlow()

    fun getDailyReport(): DailyCompanyReport {
        val txs = _transactions.value
        val income = txs.filter { it.type == FinancialType.INCOME }.sumOf { it.amount }
        val expenses = txs.filter { it.type == FinancialType.EXPENSE }.sumOf { it.amount }
        val net = income - expenses
        val dateStr = SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault()).format(Date())

        val incStr = String.format(Locale.US, "%.2f", income)
        val expStr = String.format(Locale.US, "%.2f", expenses)
        val netStr = String.format(Locale.US, "%.2f", net)

        return DailyCompanyReport(
            dateString = dateStr,
            totalRevenue = income,
            totalExpenses = expenses,
            netCashFlow = net,
            activeUsers = 1420,
            tasksCompleted = 87,
            executiveBriefingEn = "Boss, today our gross revenue stands at $$incStr with total operating expenses of $$expStr, yielding a healthy net cash flow of +$$netStr. All autonomous background workflows and self-healing agent pipelines are operating at 99.8% uptime with zero blocking outages.",
            executiveBriefingBn = "বস, আজকের মোট আয় $$incStr এবং মোট পরিচালন ব্যয় $$expStr। নিট ক্যাশ ফ্লো ধনাত্মক +$$netStr। সকল অটোনোমাস ব্যাকগ্রাউন্ড ওয়ার্কফ্লো এবং সেলফ-হিলিং এজেন্ট পাইপলাইন নিরবচ্ছিন্নভাবে সক্রিয় রয়েছে।",
            executiveBriefingBanglish = "Boss, ajker total revenue $$incStr ebong operational expense $$expStr. Net cashflow positive +$$netStr. Shob autonomous background workflows ebong self-healing agent pipelines 99.8% uptime-e smoothly run korche!",
            kpis = listOf(
                BusinessKpi("Gross Income", "$$incStr", "+18.4% vs last week", true),
                BusinessKpi("Operating Burn", "$$expStr", "-8.2% cost saved", true),
                BusinessKpi("Net Margin", "${String.format(Locale.US, "%.1f", (net / income.coerceAtLeast(1.0)) * 100)}%", "+12.1% profit", true),
                BusinessKpi("Active Agents", "4 Pipelines", "100% Healthy", true)
            )
        )
    }

    fun addTransaction(title: String, amount: Double, type: FinancialType, category: String) {
        val newTx = FinancialTransaction(
            id = System.currentTimeMillis(),
            title = title,
            amount = amount,
            type = type,
            category = category,
            timestamp = System.currentTimeMillis()
        )
        _transactions.value = listOf(newTx) + _transactions.value
    }

    fun markStrategyExecuted(strategyId: String) {
        _strategies.value = _strategies.value.map { strat ->
            if (strat.id == strategyId) strat.copy(isExecuted = true) else strat
        }
    }
}
