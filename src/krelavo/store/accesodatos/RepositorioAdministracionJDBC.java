package krelavo.store.accesodatos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import krelavo.store.basedatos.FabricaConexiones;
import krelavo.store.modelo.DatosSeccionAdministrativa;
import krelavo.store.modelo.ResumenAdministrativo;

/** Consultas y escrituras del panel; mantiene el SQL fuera de JavaFX. */
public final class RepositorioAdministracionJDBC {
    private final FabricaConexiones fabrica;
    public RepositorioAdministracionJDBC(FabricaConexiones fabrica) { this.fabrica = fabrica; }

    public ResumenAdministrativo obtenerResumen() throws SQLException {
        try (Connection c = fabrica.abrirConexion(); Statement s = c.createStatement()) {
            long productos = escalar(s, "SELECT count(*) FROM products WHERE active=1");
            long existencias = escalar(s, "SELECT coalesce(sum(stock_quantity),0) FROM products WHERE active=1");
            long pendientes = escalar(s, "SELECT count(*) FROM orders WHERE status='PENDING'");
            long clientes = escalar(s, "SELECT count(*) FROM users WHERE role='CLIENTE' AND active=1");
            long ventas = escalar(s, "SELECT coalesce(sum(total_cop),0) FROM orders WHERE status IN ('CONFIRMED','FULFILLED')");
            return new ResumenAdministrativo(productos, existencias, pendientes, clientes, ventas);
        }
    }
    private long escalar(Statement s, String sql) throws SQLException {
        try (ResultSet r=s.executeQuery(sql)) { return r.next()?r.getLong(1):0; }
    }

    public DatosSeccionAdministrativa listar(String seccion, String busqueda) throws SQLException {
        String filtro = "%" + (busqueda == null ? "" : busqueda.trim()) + "%";
        if ("Productos".equals(seccion)) return consultar(
                Arrays.asList("ID","Producto","Marca","Categoría","Precio COP","Existencias","Estado"),
                "SELECT p.id,p.name,b.name,c.name,p.price_cop,p.stock_quantity,CASE p.active WHEN 1 THEN 'Activo' ELSE 'Inactivo' END FROM products p JOIN brands b ON b.id=p.brand_id JOIN categories c ON c.id=p.category_id WHERE p.name LIKE ? OR b.name LIKE ? ORDER BY p.name", filtro, filtro);
        if ("Categorías y marcas".equals(seccion)) return consultar(
                Arrays.asList("Tipo","Nombre","Estado"),
                "SELECT tipo,nombre,estado FROM (SELECT 'Categoría' tipo,name nombre,CASE active WHEN 1 THEN 'Activa' ELSE 'Inactiva' END estado FROM categories UNION ALL SELECT 'Marca',name,CASE active WHEN 1 THEN 'Activa' ELSE 'Inactiva' END FROM brands) WHERE nombre LIKE ? ORDER BY tipo,nombre", filtro);
        if ("Inventario".equals(seccion)) return consultar(
                Arrays.asList("ID","Producto","Marca","Existencias","Estado"),
                "SELECT p.id,p.name,b.name,p.stock_quantity,CASE WHEN p.stock_quantity=0 THEN 'Agotado' WHEN p.stock_quantity<=5 THEN 'Bajo' ELSE 'Disponible' END FROM products p JOIN brands b ON b.id=p.brand_id WHERE p.name LIKE ? OR b.name LIKE ? ORDER BY p.stock_quantity,p.name", filtro, filtro);
        if ("Pedidos".equals(seccion)) return consultar(Arrays.asList("Pedido","Cliente","Estado","Total COP","Fecha"),
                "SELECT o.id,u.display_name,o.status,o.total_cop,o.created_at FROM orders o JOIN users u ON u.id=o.user_id WHERE u.display_name LIKE ? OR o.status LIKE ? ORDER BY o.created_at DESC", filtro,filtro);
        if ("Clientes".equals(seccion)) return consultar(Arrays.asList("ID","Nombre","Correo","Registro","Estado"),
                "SELECT id,display_name,email,created_at,CASE active WHEN 1 THEN 'Activo' ELSE 'Inactivo' END FROM users WHERE role='CLIENTE' AND (display_name LIKE ? OR email LIKE ?) ORDER BY created_at DESC", filtro,filtro);
        if ("Reportes".equals(seccion)) return consultar(Arrays.asList("Indicador","Valor"),
                "SELECT 'Ventas confirmadas y completadas (COP)',coalesce(sum(total_cop),0) FROM orders WHERE status IN ('CONFIRMED','FULFILLED') UNION ALL SELECT 'Pedidos totales',count(*) FROM orders UNION ALL SELECT 'Productos activos',count(*) FROM products WHERE active=1 UNION ALL SELECT 'Existencias activas',coalesce(sum(stock_quantity),0) FROM products WHERE active=1 UNION ALL SELECT 'Clientes activos',count(*) FROM users WHERE role='CLIENTE' AND active=1");
        throw new IllegalArgumentException("La sección solicitada no existe.");
    }
    private DatosSeccionAdministrativa consultar(List<String> encabezados, String sql, String... parametros) throws SQLException {
        List<List<String>> filas = new ArrayList<List<String>>();
        try (Connection c=fabrica.abrirConexion(); PreparedStatement p=c.prepareStatement(sql)) {
            for (int i=0;i<parametros.length;i++) p.setString(i+1,parametros[i]);
            try (ResultSet r=p.executeQuery()) {
                int n=r.getMetaData().getColumnCount();
                while(r.next()) { List<String> fila=new ArrayList<String>(); for(int i=1;i<=n;i++) fila.add(r.getString(i)==null?"—":r.getString(i)); filas.add(fila); }
            }
        }
        return new DatosSeccionAdministrativa(encabezados,filas);
    }
    public void actualizarProducto(long id,String nombre,long precio) throws SQLException {
        try(Connection c=fabrica.abrirConexion();PreparedStatement p=c.prepareStatement("UPDATE products SET name=?,price_cop=?,updated_at=strftime('%Y-%m-%dT%H:%M:%fZ','now') WHERE id=? AND active=1")) {
            p.setString(1,nombre);p.setLong(2,precio);p.setLong(3,id);if(p.executeUpdate()!=1)throw new SQLException("No se encontró un producto activo para actualizar.");
        }
    }
    public void crearProducto(String nombre,String marca,String categoria,long precio) throws SQLException {
        String sql="INSERT INTO products(name,brand_id,category_id,price_cop,stock_quantity,active) "
                + "SELECT ?,b.id,c.id,?,0,1 FROM brands b JOIN categories c "
                + "WHERE b.name=? AND c.name=? AND b.active=1 AND c.active=1";
        try(Connection c=fabrica.abrirConexion();PreparedStatement p=c.prepareStatement(sql)) {
            p.setString(1,nombre);p.setLong(2,precio);p.setString(3,marca);p.setString(4,categoria);
            if(p.executeUpdate()!=1)throw new SQLException("Selecciona una marca y categoría activas.");
        }
    }
    public void ajustarExistencias(long id,int cambio,String nota,long usuarioId) throws SQLException {
        try(Connection c=fabrica.abrirConexion()) {
            c.setAutoCommit(false);
            try(PreparedStatement p=c.prepareStatement("UPDATE products SET stock_quantity=stock_quantity+?,updated_at=strftime('%Y-%m-%dT%H:%M:%fZ','now') WHERE id=? AND active=1 AND stock_quantity+?>=0")) {
                p.setInt(1,cambio);p.setLong(2,id);p.setInt(3,cambio);if(p.executeUpdate()!=1)throw new SQLException("El ajuste dejaría existencias negativas o el producto no existe.");
            }
            try(PreparedStatement p=c.prepareStatement("INSERT INTO inventory_movements(product_id,user_id,movement_type,quantity_change,note) VALUES(?,?,?,?,?)")) {
                p.setLong(1,id);p.setLong(2,usuarioId);p.setString(3,cambio>0?"IN":"ADJUSTMENT");p.setInt(4,cambio);p.setString(5,nota);p.executeUpdate();
            } catch(SQLException e) { c.rollback();throw e; }
            c.commit();
        }
    }
}
