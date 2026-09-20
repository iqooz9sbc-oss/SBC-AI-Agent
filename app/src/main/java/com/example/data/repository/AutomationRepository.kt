package com.example.data.repository

import com.example.data.local.AutomationDao
import com.example.data.local.AutomationEntity
import com.example.domain.model.AutomationRule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AutomationRepository(private val dao: AutomationDao) {

    fun getRules(): Flow<List<AutomationRule>> {
        return dao.getAllRules().map { list ->
            list.map { it.toDomain() }
        }
    }

    suspend fun saveRule(rule: AutomationRule): Long {
        return dao.insertRule(AutomationEntity.fromDomain(rule))
    }

    suspend fun setRuleEnabled(id: Long, isEnabled: Boolean) {
        dao.setRuleEnabled(id, isEnabled)
    }

    suspend fun markExecuted(id: Long) {
        dao.markExecuted(id)
    }

    suspend fun deleteRule(id: Long) {
        dao.deleteRuleById(id)
    }

    suspend fun seedDefaultsIfEmpty(currentCount: Int) {
        if (currentCount == 0) {
            val defaults = listOf(
                AutomationRule(
                    title = "Night Silence & DND",
                    titleBn = "রাতের নীরব মোড",
                    description = "Mute notifications and dim screen from 11:00 PM to 7:00 AM.",
                    descriptionBn = "রাত ১১টা থেকে সকাল ৭টা পর্যন্ত নোটিফিকেশন সাইলেন্ট রাখুন।",
                    triggerText = "At 11:00 PM Daily",
                    triggerTextBn = "প্রতিদিন রাত ১১:০০ টায়",
                    actionText = "Turn on Do Not Disturb & Eco Mode",
                    actionTextBn = "ডু নট ডিস্টার্ব ও ইকো মোড সক্রিয় করো",
                    isEnabled = true
                ),
                AutomationRule(
                    title = "Morning AI Briefing",
                    titleBn = "সকালের এআই ব্রিফিং",
                    description = "Reads weather, daily schedule, and learned memory reminders.",
                    descriptionBn = "আবহাওয়া, দৈনিক সূচি এবং মেমোরির অনুস্মারক পাঠ করে শোনান।",
                    triggerText = "At 07:30 AM Daily",
                    triggerTextBn = "প্রতিদিন সকাল ০৭:৩০ টায়",
                    actionText = "Speak daily summary in chosen language",
                    actionTextBn = "নির্বাচিত ভাষায় দৈনিক সারসংক্ষেপ বলো",
                    isEnabled = true
                ),
                AutomationRule(
                    title = "Battery Guardian",
                    titleBn = "ব্যাটারি অভিভাবক",
                    description = "Switches AI model to lightweight offline heuristics when battery is below 20%.",
                    descriptionBn = "ব্যাটারি ২০% এর নিচে নামলে অফলাইন মডেলে রূপান্তর করো।",
                    triggerText = "Battery falls below 20%",
                    triggerTextBn = "ব্যাটারি ২০% এর নিচে নামলে",
                    actionText = "Switch to Offline AI & save power",
                    actionTextBn = "অফলাইন এআই তে সুইচ করে শক্তি সঞ্চয় করো",
                    isEnabled = true
                ),
                AutomationRule(
                    title = "Bangla Smart Response",
                    titleBn = "বাংলা স্মার্ট প্রতিক্রিয়া",
                    description = "Automatically detects Bengali queries and sets AI Persona to fluent Bangla.",
                    descriptionBn = "বাংলা ইনপুট পেলেই স্বয়ংক্রিয়ভাবে বাংলা বিশেষায়িত মোড সক্রিয় করো।",
                    triggerText = "Detects Bangla Unicode input",
                    triggerTextBn = "বাংলা ইউনিকোড ইনপুট শনাক্ত হলে",
                    actionText = "Engage Bengali specialized engine",
                    actionTextBn = "বাংলা বিশেষায়িত ইঞ্জিন চালু করো",
                    isEnabled = true
                )
            )
            dao.insertRules(defaults.map { AutomationEntity.fromDomain(it) })
        }
    }
}
