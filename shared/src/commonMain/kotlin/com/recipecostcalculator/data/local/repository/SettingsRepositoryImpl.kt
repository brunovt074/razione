package com.recipecostcalculator.data.local.repository

import com.recipecostcalculator.db.PizzeriaDatabase
import com.recipecostcalculator.domain.model.AppSettings
import com.recipecostcalculator.domain.model.SettingKey
import com.recipecostcalculator.domain.repository.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

class SettingsRepositoryImpl(
    private val db: PizzeriaDatabase
) : SettingsRepository {

    private val queries = db.settingsQueries
    private val refreshSignal = MutableSharedFlow<Unit>(replay = 1)

    init {
        refreshSignal.tryEmit(Unit)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeSettings(): Flow<AppSettings> = refreshSignal.flatMapLatest {
        flow { emit(getSettings()) }
    }

    override suspend fun getSettings(): AppSettings =
        withContext(Dispatchers.IO) {
            val rows = queries.selectAll().executeAsList()
            val map = mutableMapOf<String, String>()
            rows.forEach { row -> map[row.key] = row.value_ }
            map.toAppSettings()
        }

    override suspend fun updateSetting(key: SettingKey, value: String): Unit =
        withContext(Dispatchers.IO) {
            queries.upsert(key = key.key, value = value)
            refresh()
            Unit
        }

    override suspend fun updateSettings(settings: AppSettings): Unit =
        withContext(Dispatchers.IO) {
            queries.upsert(key = SettingKey.BATCH_SIZE.key, value = settings.batchSize.toString())
            queries.upsert(key = SettingKey.WASTE_FACTOR.key, value = settings.wasteFactor.toString())
            queries.upsert(key = SettingKey.DISCOUNT_PCT.key, value = settings.discountPct.toString())
            queries.upsert(key = SettingKey.ESTIMATED_MONTHLY_PRODUCTION.key, value = settings.estimatedMonthlyProduction.toString())
            queries.upsert(key = SettingKey.TARGET_MARGIN.key, value = settings.targetMargin.toString())
            queries.upsert(key = SettingKey.SEED_VERSION.key, value = settings.seedVersion.toString())
            refresh()
            Unit
        }

    private suspend fun refresh() {
        refreshSignal.emit(Unit)
    }

    private fun Map<String, String>.toAppSettings() = AppSettings(
        batchSize = this[SettingKey.BATCH_SIZE.key]?.toIntOrNull() ?: 6,
        wasteFactor = this[SettingKey.WASTE_FACTOR.key]?.toDoubleOrNull() ?: 0.0,
        discountPct = this[SettingKey.DISCOUNT_PCT.key]?.toDoubleOrNull() ?: 0.0,
        estimatedMonthlyProduction = this[SettingKey.ESTIMATED_MONTHLY_PRODUCTION.key]?.toIntOrNull() ?: 500,
        targetMargin = this[SettingKey.TARGET_MARGIN.key]?.toDoubleOrNull() ?: 0.5,
        seedVersion = this[SettingKey.SEED_VERSION.key]?.toIntOrNull() ?: 0
    )
}
