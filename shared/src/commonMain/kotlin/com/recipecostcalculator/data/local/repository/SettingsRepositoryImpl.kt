package com.recipecostcalculator.data.local.repository

import com.recipecostcalculator.db.PizzeriaDatabase
import com.recipecostcalculator.domain.model.AppSettings
import com.recipecostcalculator.domain.model.SettingKey
import com.recipecostcalculator.domain.repository.SettingsRepository
import com.recipecostcalculator.financial.domain.model.Percentage
import com.recipecostcalculator.domain.model.CostMode
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
            queries.upsert(key = SettingKey.WASTE_FACTOR.key, value = settings.wasteFactor.toDouble().toString())
            queries.upsert(key = SettingKey.COMMISSION_PCT.key, value = settings.commissionPct.toDouble().toString())
            queries.upsert(key = SettingKey.ESTIMATED_MONTHLY_PRODUCTION.key, value = settings.estimatedMonthlyProduction.toString())
            queries.upsert(key = SettingKey.TARGET_MARGIN.key, value = settings.targetMargin.toDouble().toString())
            queries.upsert(key = SettingKey.DEFAULT_COST_MODE.key, value = settings.defaultCostMode.name)
            Unit
        }

    private fun Map<String, String>.toAppSettings() = AppSettings(
        batchSize = this[SettingKey.BATCH_SIZE.key]?.toIntOrNull() ?: 6,
        wasteFactor = Percentage.fromDecimal(
            java.math.BigDecimal(this[SettingKey.WASTE_FACTOR.key]?.toDoubleOrNull() ?: 0.0)
        ),
        commissionPct = Percentage.fromDecimal(
            java.math.BigDecimal(this[SettingKey.COMMISSION_PCT.key]?.toDoubleOrNull() ?: 0.0)
        ),
        estimatedMonthlyProduction = this[SettingKey.ESTIMATED_MONTHLY_PRODUCTION.key]?.toIntOrNull() ?: 0,
        targetMargin = Percentage.fromDecimal(
            java.math.BigDecimal(this[SettingKey.TARGET_MARGIN.key]?.toDoubleOrNull() ?: 0.5)
        ),
        defaultCostMode = this[SettingKey.DEFAULT_COST_MODE.key]?.let {
            try { CostMode.valueOf(it) } catch (e: Exception) { CostMode.VARIABLE }
        } ?: CostMode.VARIABLE
    )
}