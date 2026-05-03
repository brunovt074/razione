CREATE TABLE IF NOT EXISTS fixed_cost_items (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    monthly_amount TEXT NOT NULL,
    currency TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS fixed_cost_configuration (
    id INTEGER PRIMARY KEY CHECK (id = 1),
    pizzas_per_month TEXT NOT NULL
);

INSERT OR IGNORE INTO fixed_cost_configuration(id, pizzas_per_month) VALUES (1, '0');

CREATE TABLE IF NOT EXISTS pricing_configuration (
    id INTEGER PRIMARY KEY CHECK (id = 1),
    selling_price_per_pizza TEXT NOT NULL,
    currency TEXT NOT NULL,
    commission_percent TEXT NOT NULL,
    waste_percent TEXT NOT NULL,
    target_margin_percent TEXT NOT NULL
);

INSERT OR IGNORE INTO pricing_configuration(
    id,
    selling_price_per_pizza,
    currency,
    commission_percent,
    waste_percent,
    target_margin_percent
) VALUES (1, '0', 'ARS', '0', '0', '50');

CREATE TABLE IF NOT EXISTS price_list_configuration (
    id INTEGER PRIMARY KEY CHECK (id = 1),
    cost_basis_type TEXT NOT NULL
);

INSERT OR IGNORE INTO price_list_configuration(id, cost_basis_type) VALUES (1, 'VARIABLE');

CREATE TABLE IF NOT EXISTS price_list_products (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    selling_price TEXT NOT NULL,
    currency TEXT NOT NULL,
    notes TEXT NOT NULL DEFAULT ''
);
