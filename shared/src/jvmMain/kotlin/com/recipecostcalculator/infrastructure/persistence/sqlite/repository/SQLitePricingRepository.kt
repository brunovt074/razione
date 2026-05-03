package com.recipecostcalculator.infrastructure.persistence.sqlite.repository

import com.recipecostcalculator.financial.domain.model.Money
import com.recipecostcalculator.infrastructure.persistence.sqlite.SQLiteDatabase
import com.recipecostcalculator.pricing.domain.model.CostBasisType
import com.recipecostcalculator.pricing.domain.model.PriceListConfiguration
import com.recipecostcalculator.pricing.domain.model.PriceListProduct
import com.recipecostcalculator.pricing.domain.model.PricingConfiguration
import com.recipecostcalculator.pricing.domain.repository.PricingRepository
import java.math.BigDecimal
import java.sql.ResultSet
import java.sql.Statement

class SQLitePricingRepository(
    private val database: SQLiteDatabase,
) : PricingRepository {

    override fun savePricingConfiguration(configuration: PricingConfiguration): PricingConfiguration {
        return database.withConnection { connection ->
            connection.prepareStatement(
                "INSERT INTO pricing_configuration(id, selling_price_per_pizza, currency, commission_percent, waste_percent, target_margin_percent) VALUES (1, ?, ?, ?, ?, ?) ON CONFLICT(id) DO UPDATE SET selling_price_per_pizza = excluded.selling_price_per_pizza, currency = excluded.currency, commission_percent = excluded.commission_percent, waste_percent = excluded.waste_percent, target_margin_percent = excluded.target_margin_percent",
            ).use { statement ->
                statement.setString(1, configuration.sellingPricePerPizza.amount.toPlainString())
                statement.setString(2, configuration.sellingPricePerPizza.currency)
                statement.setString(3, configuration.commissionPercent.toPlainString())
                statement.setString(4, configuration.wastePercent.toPlainString())
                statement.setString(5, configuration.targetMarginPercent.toPlainString())
                statement.executeUpdate()
            }
            configuration
        }
    }

    override fun getPricingConfiguration(): PricingConfiguration {
        return database.withConnection { connection ->
            connection.prepareStatement(
                "SELECT selling_price_per_pizza, currency, commission_percent, waste_percent, target_margin_percent FROM pricing_configuration WHERE id = 1",
            ).use { statement ->
                statement.executeQuery().use { resultSet ->
                    if (resultSet.next()) {
                        mapPricingConfiguration(resultSet)
                    } else {
                        val defaults = PricingConfiguration.defaults()
                        savePricingConfiguration(defaults)
                        defaults
                    }
                }
            }
        }
    }

    override fun savePriceListConfiguration(configuration: PriceListConfiguration): PriceListConfiguration {
        return database.withConnection { connection ->
            connection.prepareStatement(
                "INSERT INTO price_list_configuration(id, cost_basis_type) VALUES (1, ?) ON CONFLICT(id) DO UPDATE SET cost_basis_type = excluded.cost_basis_type",
            ).use { statement ->
                statement.setString(1, configuration.costBasisType.name)
                statement.executeUpdate()
            }
            configuration
        }
    }

    override fun getPriceListConfiguration(): PriceListConfiguration {
        return database.withConnection { connection ->
            connection.prepareStatement(
                "SELECT cost_basis_type FROM price_list_configuration WHERE id = 1",
            ).use { statement ->
                statement.executeQuery().use { resultSet ->
                    if (resultSet.next()) {
                        PriceListConfiguration(
                            costBasisType = CostBasisType.valueOf(resultSet.getString("cost_basis_type")),
                        )
                    } else {
                        val defaults = PriceListConfiguration.defaults()
                        savePriceListConfiguration(defaults)
                        defaults
                    }
                }
            }
        }
    }

    override fun saveProduct(product: PriceListProduct): PriceListProduct {
        return if (product.id == 0L) {
            createProduct(product)
        } else {
            updateProduct(product)
        }
    }

    override fun findAllProducts(): List<PriceListProduct> {
        return database.withConnection { connection ->
            connection.prepareStatement(
                "SELECT id, name, selling_price, currency, notes FROM price_list_products ORDER BY name ASC",
            ).use { statement ->
                statement.executeQuery().use { resultSet ->
                    val products = mutableListOf<PriceListProduct>()
                    while (resultSet.next()) {
                        products += mapProduct(resultSet)
                    }
                    products
                }
            }
        }
    }

    private fun createProduct(product: PriceListProduct): PriceListProduct {
        return database.withConnection { connection ->
            connection.prepareStatement(
                "INSERT INTO price_list_products(name, selling_price, currency, notes) VALUES (?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS,
            ).use { statement ->
                statement.setString(1, product.name)
                statement.setString(2, product.sellingPrice.amount.toPlainString())
                statement.setString(3, product.sellingPrice.currency)
                statement.setString(4, product.notes)
                statement.executeUpdate()

                statement.generatedKeys.use { keys ->
                    check(keys.next()) { "No se pudo recuperar el id generado para producto" }
                    product.copy(id = keys.getLong(1))
                }
            }
        }
    }

    private fun updateProduct(product: PriceListProduct): PriceListProduct {
        val productId = product.id
        require(productId > 0) { "Producto sin id persistido" }
        return database.withConnection { connection ->
            connection.prepareStatement(
                "UPDATE price_list_products SET name = ?, selling_price = ?, currency = ?, notes = ? WHERE id = ?",
            ).use { statement ->
                statement.setString(1, product.name)
                statement.setString(2, product.sellingPrice.amount.toPlainString())
                statement.setString(3, product.sellingPrice.currency)
                statement.setString(4, product.notes)
                statement.setLong(5, productId)
                val updatedRows = statement.executeUpdate()
                require(updatedRows > 0) { "Producto no encontrado: $productId" }
                product
            }
        }
    }

    private fun mapPricingConfiguration(resultSet: ResultSet): PricingConfiguration {
        return PricingConfiguration(
            sellingPricePerPizza = Money.of(
                value = resultSet.getString("selling_price_per_pizza"),
                currency = resultSet.getString("currency"),
            ),
            commissionPercent = BigDecimal(resultSet.getString("commission_percent")),
            wastePercent = BigDecimal(resultSet.getString("waste_percent")),
            targetMarginPercent = BigDecimal(resultSet.getString("target_margin_percent")),
        )
    }

    private fun mapProduct(resultSet: ResultSet): PriceListProduct {
        return PriceListProduct(
            id = resultSet.getLong("id"),
            name = resultSet.getString("name"),
            sellingPrice = Money.of(
                value = resultSet.getString("selling_price"),
                currency = resultSet.getString("currency"),
            ),
            notes = resultSet.getString("notes") ?: "",
        )
    }
}
