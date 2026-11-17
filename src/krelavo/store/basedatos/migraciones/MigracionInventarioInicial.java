package krelavo.store.basedatos.migraciones;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import krelavo.store.basedatos.MigracionEsquema;
import krelavo.store.modelo.Producto;
import krelavo.store.modelo.CatalogoProductos;

/** Establece existencias iniciales sin modificar cantidades ya existentes. */
public final class MigracionInventarioInicial implements MigracionEsquema {
    private static final int VERSION_MIGRACION = 3;
    private static final int EXISTENCIAS_INICIALES = 10;
    private static final String NOTA_CARGA = "Carga inicial de inventario: 10 unidades";

    @Override
    public int obtenerVersion() {
        return VERSION_MIGRACION;
    }

    @Override
    public void aplicar(Connection conexion) throws SQLException {
        List<Producto> productos = CatalogoProductos.crearProductos();
        for (Producto producto : productos) {
            actualizarExistenciasIniciales(conexion, producto);
        }
    }

    private void actualizarExistenciasIniciales(Connection conexion, Producto producto)
            throws SQLException {
        long identificadorProducto;
        int existenciasActuales;
        String consulta = "SELECT p.id, p.stock_quantity FROM products p "
                + "JOIN brands b ON b.id = p.brand_id "
                + "JOIN categories c ON c.id = p.category_id "
                + "WHERE p.name = ? AND b.name = ? AND c.name = ?";
        try (PreparedStatement sentencia = conexion.prepareStatement(consulta)) {
            sentencia.setString(1, producto.obtenerNombre());
            sentencia.setString(2, producto.obtenerMarca());
            sentencia.setString(3, producto.obtenerCategoria());
            try (ResultSet resultado = sentencia.executeQuery()) {
                if (!resultado.next()) {
                    throw new SQLException("No se encontró el producto del catálogo: "
                            + producto.obtenerNombre() + " (" + producto.obtenerMarca() + ").");
                }
                identificadorProducto = resultado.getLong("id");
                existenciasActuales = resultado.getInt("stock_quantity");
            }
        }

        if (existenciasActuales != 0) {
            return;
        }

        String actualizacion = "UPDATE products SET stock_quantity = ?, "
                + "updated_at = strftime('%Y-%m-%dT%H:%M:%fZ','now') "
                + "WHERE id = ? AND stock_quantity = 0";
        try (PreparedStatement sentencia = conexion.prepareStatement(actualizacion)) {
            sentencia.setInt(1, EXISTENCIAS_INICIALES);
            sentencia.setLong(2, identificadorProducto);
            if (sentencia.executeUpdate() == 1) {
                registrarEntradaInventario(conexion, identificadorProducto);
            }
        }
    }

    private void registrarEntradaInventario(Connection conexion, long identificadorProducto)
            throws SQLException {
        String insercion = "INSERT INTO inventory_movements "
                + "(product_id, movement_type, quantity_change, note) "
                + "VALUES (?, 'IN', ?, ?)";
        try (PreparedStatement sentencia = conexion.prepareStatement(insercion)) {
            sentencia.setLong(1, identificadorProducto);
            sentencia.setInt(2, EXISTENCIAS_INICIALES);
            sentencia.setString(3, NOTA_CARGA);
            sentencia.executeUpdate();
        }
    }
}
