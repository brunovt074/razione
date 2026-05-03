package com.recipecostcalculator.infrastructure.persistence.sqlite.repository

import com.recipecostcalculator.financial.domain.model.Money
import com.recipecostcalculator.fixedcost.domain.model.FixedCostItem
import com.recipecostcalculator.fixedcost.domain.model.ProductionConfiguration
import com.recipecostcalculator.fixedcost.domain.repository.FixedCostRepository
import com.recipecostcalculator.infrastructure.persistence.sqlite.SQLiteDatabase
import java.math.BigDecimal
import java.sql.ResultSet
import java.sql.Statement

class SQLiteFixedCostRepository(
    private val database: SQLiteDatabase,
) : FixedCostRepository {

    override fun saveItem(item: FixedCostItem): FixedCostItem {
        return if (item.id == 0L) {
            create(item)
        } else {
            update(item)
        }
    }

    override fun findAllItems(): List<FixedCostItem> {
        return database.withConnection { connection ->
            connection.prepareStatement(
                "SELECT id, name, monthly_amount, currency FROM fixed_cost_items ORDER BY name ASC",
            ).use { statement ->
                statement.executeQuery().use { resultSet ->
                    val items = mutableListOf<FixedCostItem>()
                    while (resultSet.next()) {
                        items += mapFixedCostItem(resultSet)
                    }
                    items
                }
            }
        }
    }

    override fun saveProductionConfiguration(configuration: ProductionConfiguration): ProductionConfiguration {
        return database.withConnection { connection ->
            connection.prepareStatement(
                "INSERT INTO fixed_cost_configuration(id, pizzas_per_month) VALUES (1, ?) ON CONFLICT(id) DO UPDATE SET pizzas_per_month = excluded.pizzas_per_month",
            ).use { statement ->
                statement.setString(1, configuration.pizzasPerMonth.toPlainString())
                statement.executeUpdate()
            }
            configuration
        }
    }

    override fun getProductionConfiguration(): ProductionConfiguration {
        return database.withConnection { connection ->
            connection.prepareStatement(
                "SELECT pizzas_per_month FROM fixed_cost_configuration WHERE id = 1",
            ).use { statement ->
                statement.executeQuery().use { resultSet ->
                    if (resultSet.next()) {
                        ProductionConfiguration(
                            pizzasPerMonth = BigDecimal(resultSet.getString("pizzas_per_month")),
                        )
                    } else {
                        ProductionConfiguration(pizzasPerMonth = BigDecimal.ZERO)
                    }
                }
            }
        }
    }

    private fun create(item: FixedCostItem): FixedCostItem {
        return database.withConnection { connection ->
            connection.prepareStatement(
                "INSERT INTO fixed_cost_items(name, monthly_amount, currency) VALUES (?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS,
            ).use { statement ->
                statement.setString(1, item.name)
                statement.setString(2, item.monthlyAmount.amount.toPlainString())
                statement.setString(3, item.monthlyAmount.currency)
                statement.executeUpdate()

                statement.generatedKeys.use { keys ->
                    check(keys.next()) { "No se pudo recuperar el id generado para gasto fijo" }
                    item.copy(id = keys.getLong(1))
                }
            }
        }
    }

    private fun update(item: FixedCostItem): FixedCostItem {
        val itemId = item.id
        require(itemId > 0) { "Gasto fijo sin id persistido" }
        return database.withConnection { connection ->
            connection.prepareStatement(
                "UPDATE fixed_cost_items SET name = ?, monthly_amount = ?, currency = ? WHERE id = ?",
            ).use { statement ->
                statement.setString(1, item.name)
                statement.setString(2, item.monthlyAmount.amount.toPlainString())
                statement.setString(3, item.monthlyAmount.currency)
                statement.setLong(4, itemId)
                val updatedRows = statement.executeUpdate()
                require(updatedRows > 0) { "Gasto fijo no encontrado: $itemId" }
                item
            }
        }
    }

    private fun mapFixedCostItem(resultSet: ResultSet): FixedCostItem {
        return FixedCostItem(
            id = resultSet.getLong("id"),
            name = resultSet.getString("name"),
            monthlyAmount = Money.of(
                value = resultSet.getString("monthly_amount"),
                currency = resultSet.getString("currency"),
            ),
        )
    }
}
