package krelavo.store.basedatos.migraciones;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import krelavo.store.basedatos.MigracionEsquema;
import krelavo.store.modelo.Producto;
import krelavo.store.modelo.CatalogoProductos;

/** Carga el catálogo original una sola vez, agrega los registros faltantes y conserva los datos existentes. */
public final class MigracionCatalogoInicial implements MigracionEsquema {
    public int obtenerVersion() {
        return 2;
    }

    public void aplicar(Connection conexion) throws SQLException {
        List<Producto> productos = CatalogoProductos.crearProductos();
        for (Producto producto : productos) {
            long identificadorMarca = asegurarRegistroCatalogo(conexion, "brands", producto.obtenerMarca());
            long identificadorCategoria = asegurarRegistroCatalogo(conexion, "categories", producto.obtenerCategoria());
            insertarProductoSiFalta(conexion, producto, identificadorMarca, identificadorCategoria);
        }
    }

    private long asegurarRegistroCatalogo(Connection conexion, String tabla, String nombre) throws SQLException {
        if (!"brands".equals(tabla) && !"categories".equals(tabla)) {
            throw new IllegalArgumentException("Tabla de catálogo no permitida.");
        }
        try (PreparedStatement insercion = conexion.prepareStatement(
                "INSERT OR IGNORE INTO " + tabla + " (name) VALUES (?)")) {
            insercion.setString(1, nombre);
            insercion.executeUpdate();
        }
        try (PreparedStatement consulta = conexion.prepareStatement(
                "SELECT id FROM " + tabla + " WHERE name = ?")) {
            consulta.setString(1, nombre);
            try (ResultSet resultado = consulta.executeQuery()) {
                if (!resultado.next()) {
                    throw new SQLException("No se encontró el registro de catálogo: " + nombre);
                }
                return resultado.getLong(1);
            }
        }
    }

    private void insertarProductoSiFalta(Connection conexion, Producto producto,
            long identificadorMarca, long identificadorCategoria) throws SQLException {
        String sql = "INSERT OR IGNORE INTO products "
                + "(name, brand_id, category_id, price_cop, stock_quantity, image_path) "
                + "VALUES (?, ?, ?, ?, 0, ?)";
        try (PreparedStatement insercion = conexion.prepareStatement(sql)) {
            insercion.setString(1, producto.obtenerNombre());
            insercion.setLong(2, identificadorMarca);
            insercion.setLong(3, identificadorCategoria);
            insercion.setLong(4, producto.obtenerPrecioCop());
            insercion.setString(5, producto.obtenerRutaImagen());
            insercion.executeUpdate();
        }
    }
}
