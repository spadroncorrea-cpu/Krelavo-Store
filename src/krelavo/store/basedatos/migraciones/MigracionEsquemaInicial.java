package krelavo.store.basedatos.migraciones;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import krelavo.store.basedatos.MigracionEsquema;

/** Crea el esquema relacional inicial y sus índices de consulta. */
public final class MigracionEsquemaInicial implements MigracionEsquema {
    private static final List<String> SQL = Arrays.asList(
        "CREATE TABLE users (id INTEGER PRIMARY KEY AUTOINCREMENT, display_name TEXT NOT NULL CHECK(length(trim(display_name)) > 0), email TEXT NOT NULL COLLATE NOCASE UNIQUE CHECK(length(trim(email)) > 0), password_hash TEXT NOT NULL, role TEXT NOT NULL CHECK(role IN ('ADMIN','CLIENTE')), active INTEGER NOT NULL DEFAULT 1 CHECK(active IN (0,1)), created_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ','now')))",
        "CREATE TABLE categories (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL UNIQUE CHECK(length(trim(name)) > 0), active INTEGER NOT NULL DEFAULT 1 CHECK(active IN (0,1)), created_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ','now')))",
        "CREATE TABLE brands (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL UNIQUE CHECK(length(trim(name)) > 0), active INTEGER NOT NULL DEFAULT 1 CHECK(active IN (0,1)), created_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ','now')))",
        "CREATE TABLE products (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL CHECK(length(trim(name)) > 0), description TEXT, brand_id INTEGER NOT NULL, category_id INTEGER NOT NULL, price_cop INTEGER NOT NULL CHECK(price_cop >= 0), stock_quantity INTEGER NOT NULL DEFAULT 0 CHECK(stock_quantity >= 0), image_path TEXT, active INTEGER NOT NULL DEFAULT 1 CHECK(active IN (0,1)), created_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ','now')), updated_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ','now')), UNIQUE(brand_id, name), FOREIGN KEY(brand_id) REFERENCES brands(id) ON UPDATE CASCADE ON DELETE RESTRICT, FOREIGN KEY(category_id) REFERENCES categories(id) ON UPDATE CASCADE ON DELETE RESTRICT)",
        "CREATE TABLE favorites (user_id INTEGER NOT NULL, product_id INTEGER NOT NULL, created_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ','now')), PRIMARY KEY(user_id, product_id), FOREIGN KEY(user_id) REFERENCES users(id) ON UPDATE CASCADE ON DELETE CASCADE, FOREIGN KEY(product_id) REFERENCES products(id) ON UPDATE CASCADE ON DELETE CASCADE)",
        "CREATE TABLE carts (id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER NOT NULL UNIQUE, created_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ','now')), updated_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ','now')), FOREIGN KEY(user_id) REFERENCES users(id) ON UPDATE CASCADE ON DELETE CASCADE)",
        "CREATE TABLE cart_items (cart_id INTEGER NOT NULL, product_id INTEGER NOT NULL, quantity INTEGER NOT NULL CHECK(quantity > 0), added_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ','now')), PRIMARY KEY(cart_id, product_id), FOREIGN KEY(cart_id) REFERENCES carts(id) ON UPDATE CASCADE ON DELETE CASCADE, FOREIGN KEY(product_id) REFERENCES products(id) ON UPDATE CASCADE ON DELETE RESTRICT)",
        "CREATE TABLE orders (id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER NOT NULL, status TEXT NOT NULL DEFAULT 'PENDING' CHECK(status IN ('PENDING','CONFIRMED','CANCELLED','FULFILLED')), total_cop INTEGER NOT NULL CHECK(total_cop >= 0), created_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ','now')), updated_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ','now')), FOREIGN KEY(user_id) REFERENCES users(id) ON UPDATE CASCADE ON DELETE RESTRICT)",
        "CREATE TABLE order_items (id INTEGER PRIMARY KEY AUTOINCREMENT, order_id INTEGER NOT NULL, product_id INTEGER, product_name_snapshot TEXT NOT NULL CHECK(length(trim(product_name_snapshot)) > 0), unit_price_cop INTEGER NOT NULL CHECK(unit_price_cop >= 0), quantity INTEGER NOT NULL CHECK(quantity > 0), line_total_cop INTEGER NOT NULL CHECK(line_total_cop >= 0), FOREIGN KEY(order_id) REFERENCES orders(id) ON UPDATE CASCADE ON DELETE CASCADE, FOREIGN KEY(product_id) REFERENCES products(id) ON UPDATE CASCADE ON DELETE SET NULL)",
        "CREATE TABLE inventory_movements (id INTEGER PRIMARY KEY AUTOINCREMENT, product_id INTEGER NOT NULL, order_id INTEGER, user_id INTEGER, movement_type TEXT NOT NULL CHECK(movement_type IN ('IN','OUT','ADJUSTMENT')), quantity_change INTEGER NOT NULL CHECK(quantity_change <> 0), note TEXT, created_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ','now')), FOREIGN KEY(product_id) REFERENCES products(id) ON UPDATE CASCADE ON DELETE RESTRICT, FOREIGN KEY(order_id) REFERENCES orders(id) ON UPDATE CASCADE ON DELETE SET NULL, FOREIGN KEY(user_id) REFERENCES users(id) ON UPDATE CASCADE ON DELETE SET NULL)",
        "CREATE INDEX idx_products_category ON products(category_id)",
        "CREATE INDEX idx_products_brand ON products(brand_id)",
        "CREATE INDEX idx_products_active ON products(active)",
        "CREATE INDEX idx_favorites_product ON favorites(product_id)",
        "CREATE INDEX idx_cart_items_product ON cart_items(product_id)",
        "CREATE INDEX idx_orders_user_created ON orders(user_id, created_at)",
        "CREATE INDEX idx_orders_status_created ON orders(status, created_at)",
        "CREATE INDEX idx_order_items_product ON order_items(product_id)",
        "CREATE INDEX idx_inventory_product_created ON inventory_movements(product_id, created_at)",
        "CREATE INDEX idx_inventory_order ON inventory_movements(order_id)"
    );

    public int obtenerVersion() {
        return 1;
    }

    public void aplicar(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            for (String sql : SQL) {
                statement.execute(sql);
            }
        }
    }
}
