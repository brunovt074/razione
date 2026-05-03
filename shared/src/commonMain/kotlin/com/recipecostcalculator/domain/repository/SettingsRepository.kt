package com.recipecostcalculator.domain.repository

import com.recipecostcalculator.domain.model.AppSettings
import com.recipecostcalculator.domain.model.SettingKey
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun observeSettings(): Flow<AppSettings>
    suspend fun getSettings(): AppSettings
    suspend fun updateSetting(key: SettingKey, value: String)
    suspend fun updateSettings(settings: AppSettings)
}