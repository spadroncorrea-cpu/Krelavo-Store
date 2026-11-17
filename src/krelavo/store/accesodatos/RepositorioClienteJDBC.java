package krelavo.store.accesodatos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import krelavo.store.basedatos.FabricaConexiones;
import krelavo.store.modelo.DireccionCliente;
import krelavo.store.modelo.LineaCarrito;
import krelavo.store.modelo.PedidoCliente;
import krelavo.store.modelo.Producto;
import krelavo.store.modelo.Rol;
import krelavo.store.modelo.Usuario;

/** Persistencia JDBC de carrito, favoritos, perfil, direcciones y pedidos del cliente. */
public final class RepositorioClienteJDBC {
    private final FabricaConexiones fabrica;
    public RepositorioClienteJDBC(FabricaConexiones fabrica) {
        if (fabrica == null) throw new IllegalArgumentException("La fábrica de conexiones es obligatoria.");
        this.fabrica=fabrica;
    }

    public List<Producto> listarProductos() throws SQLException {
        List<Producto> productos=new ArrayList<Producto>();
        String sql="SELECT p.id,p.name,b.name marca,p.price_cop,c.name categoria,p.image_path,p.stock_quantity "
                +"FROM products p JOIN brands b ON b.id=p.brand_id JOIN categories c ON c.id=p.category_id "
                +"WHERE p.active=1 AND b.active=1 AND c.active=1 ORDER BY p.id";
        try(Connection c=fabrica.abrirConexion();Statement s=c.createStatement();ResultSet r=s.executeQuery(sql)){
            while(r.next())productos.add(leerProducto(r));
        }
        return productos;
    }

    public List<Producto> listarFavoritos(long usuarioId) throws SQLException {
        List<Producto> productos=new ArrayList<Producto>();
        String sql="SELECT p.id,p.name,b.name marca,p.price_cop,c.name categoria,p.image_path,p.stock_quantity "
                +"FROM favorites f JOIN products p ON p.id=f.product_id JOIN brands b ON b.id=p.brand_id "
                +"JOIN categories c ON c.id=p.category_id WHERE f.user_id=? AND p.active=1 ORDER BY f.created_at DESC";
        try(Connection c=fabrica.abrirConexion();PreparedStatement p=c.prepareStatement(sql)){
            p.setLong(1,usuarioId);try(ResultSet r=p.executeQuery()){while(r.next()){Producto producto=leerProducto(r);producto.establecerFavorito(true);productos.add(producto);}}
        }
        return productos;
    }

    public boolean cambiarFavorito(long usuarioId,long productoId) throws SQLException {
        try(Connection c=fabrica.abrirConexion()){
            c.setAutoCommit(false);
            try(PreparedStatement existe=c.prepareStatement("SELECT 1 FROM favorites WHERE user_id=? AND product_id=?")){
                existe.setLong(1,usuarioId);existe.setLong(2,productoId);
                try(ResultSet r=existe.executeQuery()){
                    if(r.next()){
                        try(PreparedStatement borrar=c.prepareStatement("DELETE FROM favorites WHERE user_id=? AND product_id=?")){borrar.setLong(1,usuarioId);borrar.setLong(2,productoId);borrar.executeUpdate();}
                        c.commit();return false;
                    }
                }
            }
            try(PreparedStatement insertar=c.prepareStatement("INSERT INTO favorites(user_id,product_id) SELECT ?,id FROM products WHERE id=? AND active=1")){
                insertar.setLong(1,usuarioId);insertar.setLong(2,productoId);
                if(insertar.executeUpdate()!=1)throw new SQLException("El producto ya no está disponible.");
            }
            c.commit();return true;
        }catch(SQLException ex){throw ex;}
    }

    public List<LineaCarrito> obtenerCarrito(long usuarioId) throws SQLException {
        List<LineaCarrito> lineas=new ArrayList<LineaCarrito>();
        String sql="SELECT p.id,p.name,b.name marca,p.price_cop,c.name categoria,p.image_path,p.stock_quantity,ci.quantity "
                +"FROM carts ca JOIN cart_items ci ON ci.cart_id=ca.id JOIN products p ON p.id=ci.product_id "
                +"JOIN brands b ON b.id=p.brand_id JOIN categories c ON c.id=p.category_id "
                +"WHERE ca.user_id=? AND p.active=1 ORDER BY ci.added_at";
        try(Connection c=fabrica.abrirConexion();PreparedStatement p=c.prepareStatement(sql)){
            p.setLong(1,usuarioId);try(ResultSet r=p.executeQuery()){while(r.next())lineas.add(new LineaCarrito(leerProducto(r),r.getInt("quantity")));}
        }
        return lineas;
    }

    public int obtenerUnidadesCarrito(long usuarioId) throws SQLException {
        try(Connection c=fabrica.abrirConexion();PreparedStatement p=c.prepareStatement(
                "SELECT coalesce(sum(ci.quantity),0) FROM carts ca JOIN cart_items ci ON ci.cart_id=ca.id WHERE ca.user_id=?")){
            p.setLong(1,usuarioId);try(ResultSet r=p.executeQuery()){return r.next()?r.getInt(1):0;}
        }
    }

    public void agregarAlCarrito(long usuarioId,long productoId,int cantidad) throws SQLException {
        if(cantidad<1)throw new IllegalArgumentException("La cantidad debe ser al menos una unidad.");
        try(Connection c=fabrica.abrirConexion()){
            c.setAutoCommit(false);
            try{
                int existencias;
                try(PreparedStatement p=c.prepareStatement("SELECT stock_quantity FROM products WHERE id=? AND active=1")){
                    p.setLong(1,productoId);try(ResultSet r=p.executeQuery()){if(!r.next())throw new SQLException("El producto no está disponible.");existencias=r.getInt(1);}
                }
                long carrito=obtenerOCrearCarrito(c,usuarioId);
                int cantidadActual=0;
                try(PreparedStatement p=c.prepareStatement("SELECT quantity FROM cart_items WHERE cart_id=? AND product_id=?")){
                    p.setLong(1,carrito);p.setLong(2,productoId);try(ResultSet r=p.executeQuery()){if(r.next())cantidadActual=r.getInt(1);}
                }
                if((long)cantidadActual+cantidad>existencias)throw new SQLException("Solo hay "+existencias+" unidades disponibles para este producto.");
                try(PreparedStatement p=c.prepareStatement("INSERT INTO cart_items(cart_id,product_id,quantity) VALUES(?,?,?) ON CONFLICT(cart_id,product_id) DO UPDATE SET quantity=excluded.quantity,added_at=strftime('%Y-%m-%dT%H:%M:%fZ','now')")){
                    p.setLong(1,carrito);p.setLong(2,productoId);p.setInt(3,cantidadActual+cantidad);p.executeUpdate();
                }
                actualizarCarrito(c,carrito);c.commit();
            }catch(SQLException|RuntimeException ex){c.rollback();throw ex;}
        }
    }

    public void actualizarCantidadCarrito(long usuarioId,long productoId,int cantidad) throws SQLException {
        if(cantidad<1)throw new IllegalArgumentException("La cantidad no puede ser inferior a uno.");
        try(Connection c=fabrica.abrirConexion()){
            c.setAutoCommit(false);
            try{
                long carrito=obtenerCarrito(c,usuarioId);
                int existencias;
                try(PreparedStatement p=c.prepareStatement("SELECT stock_quantity FROM products WHERE id=? AND active=1")){
                    p.setLong(1,productoId);try(ResultSet r=p.executeQuery()){if(!r.next())throw new SQLException("El producto ya no está disponible.");existencias=r.getInt(1);}
                }
                if(cantidad>existencias)throw new SQLException("La cantidad supera las existencias disponibles ("+existencias+").");
                try(PreparedStatement p=c.prepareStatement("UPDATE cart_items SET quantity=? WHERE cart_id=? AND product_id=?")){
                    p.setInt(1,cantidad);p.setLong(2,carrito);p.setLong(3,productoId);if(p.executeUpdate()!=1)throw new SQLException("El artículo ya no está en el carrito.");
                }
                actualizarCarrito(c,carrito);c.commit();
            }catch(SQLException|RuntimeException ex){c.rollback();throw ex;}
        }
    }

    public void quitarDelCarrito(long usuarioId,long productoId) throws SQLException {
        try(Connection c=fabrica.abrirConexion()){
            long carrito=obtenerCarrito(c,usuarioId);
            try(PreparedStatement p=c.prepareStatement("DELETE FROM cart_items WHERE cart_id=? AND product_id=?")){p.setLong(1,carrito);p.setLong(2,productoId);p.executeUpdate();}
            actualizarCarrito(c,carrito);
        }
    }

    private long obtenerOCrearCarrito(Connection c,long usuarioId)throws SQLException{
        try(PreparedStatement p=c.prepareStatement("INSERT OR IGNORE INTO carts(user_id) VALUES(?)")){p.setLong(1,usuarioId);p.executeUpdate();}
        return obtenerCarrito(c,usuarioId);
    }
    private long obtenerCarrito(Connection c,long usuarioId)throws SQLException{
        try(PreparedStatement p=c.prepareStatement("SELECT id FROM carts WHERE user_id=?")){p.setLong(1,usuarioId);try(ResultSet r=p.executeQuery()){if(!r.next())throw new SQLException("No existe el carrito del usuario.");return r.getLong(1);}}
    }
    private void actualizarCarrito(Connection c,long carrito)throws SQLException{
        try(PreparedStatement p=c.prepareStatement("UPDATE carts SET updated_at=strftime('%Y-%m-%dT%H:%M:%fZ','now') WHERE id=?")){p.setLong(1,carrito);p.executeUpdate();}
    }

    public List<DireccionCliente> listarDirecciones(long usuarioId)throws SQLException{
        List<DireccionCliente> direcciones=new ArrayList<DireccionCliente>();
        try(Connection c=fabrica.abrirConexion();PreparedStatement p=c.prepareStatement("SELECT * FROM direcciones WHERE usuario_id=? ORDER BY es_principal DESC,fecha_creacion DESC")){
            p.setLong(1,usuarioId);try(ResultSet r=p.executeQuery()){while(r.next())direcciones.add(leerDireccion(r));}
        }return direcciones;
    }

    public DireccionCliente guardarDireccion(long usuarioId,DireccionCliente direccion)throws SQLException{
        long identificadorFinal=direccion.obtenerIdentificador();
        try(Connection c=fabrica.abrirConexion()){
            c.setAutoCommit(false);
            try{
                if(direccion.esPrincipal())try(PreparedStatement p=c.prepareStatement("UPDATE direcciones SET es_principal=0 WHERE usuario_id=?")){p.setLong(1,usuarioId);p.executeUpdate();}
                if(direccion.obtenerIdentificador()>0){
                    String sql="UPDATE direcciones SET nombre_destinatario=?,telefono=?,linea_direccion=?,ciudad=?,departamento=?,indicaciones=?,es_principal=?,fecha_actualizacion=strftime('%Y-%m-%dT%H:%M:%fZ','now') WHERE id=? AND usuario_id=?";
                    try(PreparedStatement p=c.prepareStatement(sql)){asignarDireccion(p,direccion,1);p.setLong(8,direccion.obtenerIdentificador());p.setLong(9,usuarioId);if(p.executeUpdate()!=1)throw new SQLException("La dirección no pertenece a esta cuenta.");}
                }else{
                    String sql="INSERT INTO direcciones(usuario_id,nombre_destinatario,telefono,linea_direccion,ciudad,departamento,indicaciones,es_principal) VALUES(?,?,?,?,?,?,?,?)";
                    try(PreparedStatement p=c.prepareStatement(sql)){p.setLong(1,usuarioId);asignarDireccion(p,direccion,2);p.executeUpdate();}
                    try(Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT last_insert_rowid()")){if(!r.next())throw new SQLException("No se pudo obtener el identificador de la dirección.");identificadorFinal=r.getLong(1);}
                }
                c.commit();
            }catch(SQLException|RuntimeException ex){c.rollback();throw ex;}
        }
        for(DireccionCliente d:listarDirecciones(usuarioId))if(d.obtenerIdentificador()==identificadorFinal)return d;
        return null;
    }
    private void asignarDireccion(PreparedStatement p,DireccionCliente d,int inicio)throws SQLException{
        p.setString(inicio,d.obtenerDestinatario());p.setString(inicio+1,d.obtenerTelefono());p.setString(inicio+2,d.obtenerLinea());
        p.setString(inicio+3,d.obtenerCiudad());p.setString(inicio+4,d.obtenerDepartamento());p.setString(inicio+5,d.obtenerIndicaciones());p.setInt(inicio+6,d.esPrincipal()?1:0);
    }
    public void eliminarDireccion(long usuarioId,long direccionId)throws SQLException{
        try(Connection c=fabrica.abrirConexion();PreparedStatement p=c.prepareStatement("DELETE FROM direcciones WHERE id=? AND usuario_id=?")){p.setLong(1,direccionId);p.setLong(2,usuarioId);if(p.executeUpdate()!=1)throw new SQLException("La dirección no pertenece a esta cuenta.");}
    }
    public void marcarDireccionPrincipal(long usuarioId,long direccionId)throws SQLException{
        try(Connection c=fabrica.abrirConexion()){
            c.setAutoCommit(false);try(PreparedStatement q=c.prepareStatement("UPDATE direcciones SET es_principal=0 WHERE usuario_id=?")){q.setLong(1,usuarioId);q.executeUpdate();}
            try(PreparedStatement p=c.prepareStatement("UPDATE direcciones SET es_principal=1 WHERE id=? AND usuario_id=?")){p.setLong(1,direccionId);p.setLong(2,usuarioId);if(p.executeUpdate()!=1){c.rollback();throw new SQLException("La dirección no pertenece a esta cuenta.");}}
            c.commit();
        }
    }
    private DireccionCliente leerDireccion(ResultSet r)throws SQLException{return new DireccionCliente(r.getLong("id"),r.getString("nombre_destinatario"),r.getString("telefono"),r.getString("linea_direccion"),r.getString("ciudad"),r.getString("departamento"),r.getString("indicaciones"),r.getInt("es_principal")==1);}

    public Usuario actualizarPerfil(long usuarioId,String nombre,String correo,String telefono)throws SQLException{
        String sql="UPDATE users SET display_name=?,email=?,telefono=? WHERE id=? AND role='CLIENTE' AND active=1";
        try(Connection c=fabrica.abrirConexion();PreparedStatement p=c.prepareStatement(sql)){
            p.setString(1,nombre);p.setString(2,correo);p.setString(3,telefono);p.setLong(4,usuarioId);
            try{if(p.executeUpdate()!=1)throw new SQLException("No se pudo actualizar el perfil del cliente.");}
            catch(SQLException ex){if(ex.getErrorCode()==19)throw new IllegalArgumentException("Ese correo ya está asociado a otra cuenta.");throw ex;}
        }
        return buscarUsuarioCliente(usuarioId);
    }
    public Usuario buscarUsuarioCliente(long usuarioId)throws SQLException{
        try(Connection c=fabrica.abrirConexion();PreparedStatement p=c.prepareStatement("SELECT id,display_name,email,role,active,telefono,created_at FROM users WHERE id=? AND role='CLIENTE'")){
            p.setLong(1,usuarioId);try(ResultSet r=p.executeQuery()){if(!r.next())throw new SQLException("No se encontró la cuenta cliente.");return leerUsuario(r);}
        }
    }
    public String obtenerHashContrasena(long usuarioId)throws SQLException{
        try(Connection c=fabrica.abrirConexion();PreparedStatement p=c.prepareStatement("SELECT password_hash FROM users WHERE id=? AND role='CLIENTE' AND active=1")){
            p.setLong(1,usuarioId);try(ResultSet r=p.executeQuery()){if(!r.next())throw new SQLException("No se encontró la cuenta cliente.");return r.getString(1);}
        }
    }
    public void actualizarHashContrasena(long usuarioId,String hash)throws SQLException{
        try(Connection c=fabrica.abrirConexion();PreparedStatement p=c.prepareStatement("UPDATE users SET password_hash=? WHERE id=? AND role='CLIENTE' AND active=1")){
            p.setString(1,hash);p.setLong(2,usuarioId);if(p.executeUpdate()!=1)throw new SQLException("No se pudo cambiar la contraseña.");
        }
    }

    public String confirmarPedido(long usuarioId,long direccionId,long envioCop)throws SQLException{
        if(envioCop<0)throw new IllegalArgumentException("El envío no puede tener un valor negativo.");
        try(Connection c=fabrica.abrirConexion()){
            c.setAutoCommit(false);
            try{
                DireccionCliente direccion;
                try(PreparedStatement p=c.prepareStatement("SELECT * FROM direcciones WHERE id=? AND usuario_id=?")){p.setLong(1,direccionId);p.setLong(2,usuarioId);try(ResultSet r=p.executeQuery()){if(!r.next())throw new SQLException("Selecciona una dirección que pertenezca a tu cuenta.");direccion=leerDireccion(r);}}
                long carrito=obtenerCarrito(c,usuarioId);
                List<LineaCarrito> lineas=new ArrayList<LineaCarrito>();
                String consulta="SELECT p.id,p.name,b.name marca,p.price_cop,c.name categoria,p.image_path,p.stock_quantity,ci.quantity FROM cart_items ci JOIN products p ON p.id=ci.product_id JOIN brands b ON b.id=p.brand_id JOIN categories c ON c.id=p.category_id WHERE ci.cart_id=? AND p.active=1 ORDER BY ci.added_at";
                try(PreparedStatement p=c.prepareStatement(consulta)){p.setLong(1,carrito);try(ResultSet r=p.executeQuery()){while(r.next())lineas.add(new LineaCarrito(leerProducto(r),r.getInt("quantity")));}}
                if(lineas.isEmpty())throw new SQLException("El carrito está vacío.");
                long subtotal=0;
                for(LineaCarrito linea:lineas){if(linea.obtenerCantidad()>linea.obtenerProducto().obtenerCantidadExistencias())throw new SQLException("No hay existencias suficientes de "+linea.obtenerProducto().obtenerNombre()+".");subtotal=Math.addExact(subtotal,linea.obtenerSubtotal());}
                long total=Math.addExact(subtotal,envioCop);
                String insertarPedido="INSERT INTO orders(user_id,status,total_cop,subtotal_cop,envio_cop,direccion_destinatario,direccion_telefono,direccion_linea,direccion_ciudad,direccion_departamento,direccion_indicaciones) VALUES(?,'PENDING',?,?,?,?,?,?,?,?,?)";
                long pedidoId;
                try(PreparedStatement p=c.prepareStatement(insertarPedido)){p.setLong(1,usuarioId);p.setLong(2,total);p.setLong(3,subtotal);p.setLong(4,envioCop);p.setString(5,direccion.obtenerDestinatario());p.setString(6,direccion.obtenerTelefono());p.setString(7,direccion.obtenerLinea());p.setString(8,direccion.obtenerCiudad());p.setString(9,direccion.obtenerDepartamento());p.setString(10,direccion.obtenerIndicaciones());p.executeUpdate();}
                try(Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT last_insert_rowid()")){if(!r.next())throw new SQLException("No se pudo generar el número de pedido.");pedidoId=r.getLong(1);}
                String codigo=String.format(java.util.Locale.ROOT,"KR-%06d",pedidoId);
                try(PreparedStatement p=c.prepareStatement("UPDATE orders SET codigo_pedido=? WHERE id=?")){p.setString(1,codigo);p.setLong(2,pedidoId);p.executeUpdate();}
                for(LineaCarrito linea:lineas){Producto producto=linea.obtenerProducto();int cantidad=linea.obtenerCantidad();long totalLinea=Math.multiplyExact(producto.obtenerPrecioCop(),cantidad);
                    try(PreparedStatement p=c.prepareStatement("INSERT INTO order_items(order_id,product_id,product_name_snapshot,marca_snapshot,unit_price_cop,quantity,line_total_cop) VALUES(?,?,?,?,?,?,?)")){p.setLong(1,pedidoId);p.setLong(2,producto.obtenerIdentificador());p.setString(3,producto.obtenerNombre());p.setString(4,producto.obtenerMarca());p.setLong(5,producto.obtenerPrecioCop());p.setInt(6,cantidad);p.setLong(7,totalLinea);p.executeUpdate();}
                    try(PreparedStatement p=c.prepareStatement("UPDATE products SET stock_quantity=stock_quantity-?,updated_at=strftime('%Y-%m-%dT%H:%M:%fZ','now') WHERE id=? AND active=1 AND stock_quantity>=?")){p.setInt(1,cantidad);p.setLong(2,producto.obtenerIdentificador());p.setInt(3,cantidad);if(p.executeUpdate()!=1)throw new SQLException("Las existencias cambiaron durante la compra. Revisa el carrito.");}
                    try(PreparedStatement p=c.prepareStatement("INSERT INTO inventory_movements(product_id,order_id,user_id,movement_type,quantity_change,note) VALUES(?,?,?,'OUT',?,'Compra '+?)")){p.setLong(1,producto.obtenerIdentificador());p.setLong(2,pedidoId);p.setLong(3,usuarioId);p.setInt(4,-cantidad);p.setString(5,codigo);p.executeUpdate();}
                }
                try(PreparedStatement p=c.prepareStatement("DELETE FROM cart_items WHERE cart_id=?")){p.setLong(1,carrito);p.executeUpdate();}
                actualizarCarrito(c,carrito);c.commit();return codigo;
            }catch(SQLException|RuntimeException ex){c.rollback();throw ex;}
        }
    }

    public List<PedidoCliente> listarPedidos(long usuarioId)throws SQLException{
        List<PedidoCliente> pedidos=new ArrayList<PedidoCliente>();
        try(Connection c=fabrica.abrirConexion();PreparedStatement p=c.prepareStatement("SELECT * FROM orders WHERE user_id=? ORDER BY created_at DESC,id DESC")){
            p.setLong(1,usuarioId);try(ResultSet r=p.executeQuery()){while(r.next())pedidos.add(leerPedido(c,r));}
        }return pedidos;
    }
    public PedidoCliente buscarPedido(long usuarioId,String codigo)throws SQLException{
        try(Connection c=fabrica.abrirConexion();PreparedStatement p=c.prepareStatement("SELECT * FROM orders WHERE user_id=? AND codigo_pedido=?")){
            p.setLong(1,usuarioId);p.setString(2,codigo);try(ResultSet r=p.executeQuery()){return r.next()?leerPedido(c,r):null;}
        }
    }
    private PedidoCliente leerPedido(Connection c,ResultSet r)throws SQLException{
        long id=r.getLong("id");List<LineaCarrito> lineas=new ArrayList<LineaCarrito>();
        String sql="SELECT oi.product_id,oi.product_name_snapshot,oi.marca_snapshot,oi.unit_price_cop,oi.quantity,oi.product_id IS NOT NULL disponible FROM order_items oi WHERE oi.order_id=? ORDER BY oi.id";
        try(PreparedStatement p=c.prepareStatement(sql)){p.setLong(1,id);try(ResultSet q=p.executeQuery()){while(q.next()){
            Producto producto=new Producto(q.getLong("product_id"),q.getString("product_name_snapshot"),q.getString("marca_snapshot"),q.getLong("unit_price_cop"),"",null,0);
            lineas.add(new LineaCarrito(producto,q.getInt("quantity")));
        }}}
        return new PedidoCliente(id,r.getString("codigo_pedido"),r.getString("created_at"),traducirEstado(r.getString("status")),r.getLong("subtotal_cop"),r.getLong("envio_cop"),r.getLong("total_cop"),r.getString("direccion_destinatario"),r.getString("direccion_telefono"),r.getString("direccion_linea"),r.getString("direccion_ciudad"),r.getString("direccion_departamento"),r.getString("direccion_indicaciones"),lineas);
    }

    private String traducirEstado(String estado){
        if("PENDING".equals(estado))return "Pendiente";
        if("CONFIRMED".equals(estado))return "Confirmado";
        if("CANCELLED".equals(estado))return "Cancelado";
        if("FULFILLED".equals(estado))return "Completado";
        return estado;
    }

    private Producto leerProducto(ResultSet r)throws SQLException{
        return new Producto(r.getLong("id"),r.getString("name"),r.getString("marca"),r.getLong("price_cop"),r.getString("categoria"),r.getString("image_path"),r.getInt("stock_quantity"));
    }
    private Usuario leerUsuario(ResultSet r)throws SQLException{
        return new Usuario(r.getLong("id"),r.getString("display_name"),r.getString("email"),Rol.valueOf(r.getString("role")),r.getInt("active")==1,r.getString("telefono"),r.getString("created_at"));
    }
}
