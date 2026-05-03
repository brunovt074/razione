package com.recipecostcalculator.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.recipecostcalculator.domain.model.AppSettings
import com.recipecostcalculator.domain.model.SettingKey
import com.recipecostcalculator.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val settings: StateFlow<AppSettings> = settingsRepository.observeSettings()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppSettings()
        )

    fun updateBatchSize(value: Int) {
        viewModelScope.launch {
            settingsRepository.updateSetting(SettingKey.BATCH_SIZE, value.toString())
        }
    }

    fun updateWasteFactor(value: Double) {
        viewModelScope.launch {
            settingsRepository.updateSetting(SettingKey.WASTE_FACTOR, value.toString())
        }
    }

    fun updateDiscountPct(value: Double) {
        viewModelScope.launch {
            settingsRepository.updateSetting(SettingKey.DISCOUNT_PCT, value.toString())
        }
    }

    fun updateEstimatedProduction(value: Int) {
        viewModelScope.launch {
            settingsRepository.updateSetting(SettingKey.ESTIMATED_MONTHLY_PRODUCTION, value.toString())
        }
    }

    fun updateTargetMargin(value: Double) {
        viewModelScope.launch {
            settingsRepository.updateSetting(SettingKey.TARGET_MARGIN, value.toString())
        }
    }
}