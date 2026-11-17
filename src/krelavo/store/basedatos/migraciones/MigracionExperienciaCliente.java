package krelavo.store.basedatos.migraciones;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import krelavo.store.basedatos.MigracionEsquema;

/** Añade perfil extendido, direcciones y datos históricos de entrega. */
public final class MigracionExperienciaCliente implements MigracionEsquema {
    @Override public int obtenerVersion() { return 5; }

    @Override public void aplicar(Connection conexion) throws SQLException {
        try (Statement sentencia = conexion.createStatement()) {
            sentencia.execute("ALTER TABLE users ADD COLUMN telefono TEXT");
            sentencia.execute("CREATE TABLE direcciones (id INTEGER PRIMARY KEY AUTOINCREMENT, usuario_id INTEGER NOT NULL, nombre_destinatario TEXT NOT NULL CHECK(length(trim(nombre_destinatario))>0), telefono TEXT NOT NULL CHECK(length(trim(telefono))>0), linea_direccion TEXT NOT NULL CHECK(length(trim(linea_direccion))>0), ciudad TEXT NOT NULL CHECK(length(trim(ciudad))>0), departamento TEXT NOT NULL CHECK(length(trim(departamento))>0), indicaciones TEXT, es_principal INTEGER NOT NULL DEFAULT 0 CHECK(es_principal IN (0,1)), fecha_creacion TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ','now')), fecha_actualizacion TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ','now')), FOREIGN KEY(usuario_id) REFERENCES users(id) ON UPDATE CASCADE ON DELETE CASCADE)");
            sentencia.execute("CREATE INDEX idx_direcciones_usuario ON direcciones(usuario_id,es_principal)");
            sentencia.execute("CREATE UNIQUE INDEX idx_direcciones_principal_unica ON direcciones(usuario_id) WHERE es_principal=1");
            sentencia.execute("ALTER TABLE orders ADD COLUMN codigo_pedido TEXT");
            sentencia.execute("ALTER TABLE orders ADD COLUMN subtotal_cop INTEGER NOT NULL DEFAULT 0 CHECK(subtotal_cop>=0)");
            sentencia.execute("ALTER TABLE orders ADD COLUMN envio_cop INTEGER NOT NULL DEFAULT 0 CHECK(envio_cop>=0)");
            sentencia.execute("ALTER TABLE orders ADD COLUMN direccion_destinatario TEXT");
            sentencia.execute("ALTER TABLE orders ADD COLUMN direccion_telefono TEXT");
            sentencia.execute("ALTER TABLE orders ADD COLUMN direccion_linea TEXT");
            sentencia.execute("ALTER TABLE orders ADD COLUMN direccion_ciudad TEXT");
            sentencia.execute("ALTER TABLE orders ADD COLUMN direccion_departamento TEXT");
            sentencia.execute("ALTER TABLE orders ADD COLUMN direccion_indicaciones TEXT");
            sentencia.execute("ALTER TABLE order_items ADD COLUMN marca_snapshot TEXT NOT NULL DEFAULT ''");
            sentencia.execute("UPDATE orders SET codigo_pedido='KR-' || printf('%06d',id), subtotal_cop=total_cop WHERE codigo_pedido IS NULL");
            sentencia.execute("CREATE UNIQUE INDEX idx_pedidos_codigo_unico ON orders(codigo_pedido)");
        }
    }
}
