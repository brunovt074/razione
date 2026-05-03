package com.recipecostcalculator.data.local.repository

import com.recipecostcalculator.db.PizzeriaDatabase
import com.recipecostcalculator.domain.model.AppSettings
import com.recipecostcalculator.domain.model.SettingKey
import com.recipecostcalculator.domain.repository.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

class SettingsRepositoryImpl(
    private val db: PizzeriaDatabase
) : SettingsRepository {

    private val queries = db.settingsQueries

    override fun observeSettings(): Flow<AppSettings> = flow {
        emit(getSettings())
    }

    override suspend fun getSettings(): AppSettings =
        withContext(Dispatchers.IO) {
            val rows = queries.selectAll().executeAsList()
            val map = mutableMapOf<String, String>()
            rows.forEach { row ->
                // SQLDelight genera propiedades con sufijo _ si hay conflictos
                map[row.key] = row.value_
            }
            map.toAppSettings()
        }

    override suspend fun updateSetting(key: SettingKey, value: String): Unit =
        withContext(Dispatchers.IO) {
            queries.upsert(key = key.key, value = value)
            Unit
        }

    override suspend fun updateSettings(settings: AppSettings): Unit =
        withContext(Dispatchers.IO) {
            queries.upsert(key = SettingKey.BATCH_SIZE.key, value = settings.batchSize.toString())
            queries.upsert(key = SettingKey.WASTE_FACTOR.key, value = settings.wasteFactor.toString())
            queries.upsert(key = SettingKey.DISCOUNT_PCT.key, value = settings.discountPct.toString())
            queries.upsert(key = SettingKey.ESTIMATED_MONTHLY_PRODUCTION.key, value = settings.estimatedMonthlyProduction.toString())
            queries.upsert(key = SettingKey.TARGET_MARGIN.key, value = settings.targetMargin.toString())
            Unit
        }

    private fun Map<String, String>.toAppSettings() = AppSettings(
        batchSize = this[SettingKey.BATCH_SIZE.key]?.toIntOrNull() ?: 6,
        wasteFactor = this[SettingKey.WASTE_FACTOR.key]?.toDoubleOrNull() ?: 0.0,
        discountPct = this[SettingKey.DISCOUNT_PCT.key]?.toDoubleOrNull() ?: 0.0,
        estimatedMonthlyProduction = this[SettingKey.ESTIMATED_MONTHLY_PRODUCTION.key]?.toIntOrNull() ?: 0,
        targetMargin = this[SettingKey.TARGET_MARGIN.key]?.toDoubleOrNull() ?: 0.5
    )
}