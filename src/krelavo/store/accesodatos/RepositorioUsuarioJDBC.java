package krelavo.store.accesodatos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import krelavo.store.basedatos.FabricaConexiones;
import krelavo.store.modelo.CredencialesUsuario;
import krelavo.store.modelo.Rol;
import krelavo.store.modelo.Usuario;

/** Implementación JDBC del acceso a usuarios. */
public final class RepositorioUsuarioJDBC implements RepositorioUsuario {
    private final FabricaConexiones fabricaConexiones;

    public RepositorioUsuarioJDBC(FabricaConexiones fabricaConexiones) {
        if (fabricaConexiones == null) {
            throw new IllegalArgumentException("La fábrica de conexiones es obligatoria.");
        }
        this.fabricaConexiones = fabricaConexiones;
    }

    @Override
    public boolean existeAdministrador() throws SQLException {
        String sql = "SELECT 1 FROM users WHERE role = 'ADMIN' LIMIT 1";
        try (Connection conexion = fabricaConexiones.abrirConexion();
                PreparedStatement sentencia = conexion.prepareStatement(sql);
                ResultSet resultado = sentencia.executeQuery()) {
            return resultado.next();
        }
    }

    @Override
    public CredencialesUsuario buscarCredenciales(String correo) throws SQLException {
        String sql = "SELECT id, display_name, email, password_hash, role, active, telefono, created_at "
                + "FROM users WHERE email = ? COLLATE NOCASE LIMIT 1";
        try (Connection conexion = fabricaConexiones.abrirConexion();
                PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, correo);
            try (ResultSet resultado = sentencia.executeQuery()) {
                if (!resultado.next()) {
                    return null;
                }
                Usuario usuario = leerUsuario(resultado);
                return new CredencialesUsuario(usuario, resultado.getString("password_hash"));
            }
        }
    }

    @Override
    public Usuario crearAdministradorInicial(String nombre, String correo, String hashContrasena)
            throws SQLException {
        try (Connection conexion = fabricaConexiones.abrirConexion()) {
            conexion.setAutoCommit(false);
            try {
                if (hayAdministrador(conexion)) {
                    throw new SQLException("La configuración inicial ya fue completada.");
                }
                Usuario administrador = insertarUsuario(conexion, nombre, correo,
                        hashContrasena, "ADMIN");
                conexion.commit();
                return administrador;
            } catch (SQLException ex) {
                revertir(conexion, ex);
                throw ex;
            } finally {
                conexion.setAutoCommit(true);
            }
        }
    }

    @Override
    public Usuario crearCliente(String nombre, String correo, String hashContrasena)
            throws SQLException {
        try (Connection conexion = fabricaConexiones.abrirConexion()) {
            return insertarUsuario(conexion, nombre, correo, hashContrasena, "CLIENTE");
        }
    }

    private boolean hayAdministrador(Connection conexion) throws SQLException {
        try (PreparedStatement sentencia = conexion.prepareStatement(
                "SELECT 1 FROM users WHERE role = 'ADMIN' LIMIT 1");
                ResultSet resultado = sentencia.executeQuery()) {
            return resultado.next();
        }
    }

    private Usuario insertarUsuario(Connection conexion, String nombre, String correo,
            String hashContrasena, String rol) throws SQLException {
        String sql = "INSERT INTO users (display_name, email, password_hash, role) "
                + "VALUES (?, ?, ?, ?)";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, nombre);
            sentencia.setString(2, correo);
            sentencia.setString(3, hashContrasena);
            sentencia.setString(4, rol);
            sentencia.executeUpdate();
        }
        try (Statement sentencia = conexion.createStatement();
                ResultSet resultado = sentencia.executeQuery("SELECT last_insert_rowid()")) {
            if (!resultado.next()) {
                throw new SQLException("No se pudo obtener el identificador de la cuenta creada.");
            }
            return new Usuario(resultado.getLong(1), nombre, correo,
                    Rol.valueOf(rol), true);
        }
    }

    private Usuario leerUsuario(ResultSet resultado) throws SQLException {
        return new Usuario(resultado.getLong("id"), resultado.getString("display_name"),
                resultado.getString("email"), Rol.valueOf(resultado.getString("role")),
                resultado.getInt("active") == 1, resultado.getString("telefono"),
                resultado.getString("created_at"));
    }

    private void revertir(Connection conexion, SQLException errorOriginal) {
        try {
            conexion.rollback();
        } catch (SQLException errorReversion) {
            errorOriginal.addSuppressed(errorReversion);
        }
    }
}
