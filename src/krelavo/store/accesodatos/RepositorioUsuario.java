package krelavo.store.accesodatos;

import java.sql.SQLException;
import krelavo.store.modelo.CredencialesUsuario;
import krelavo.store.modelo.Usuario;

/** Acceso persistente a cuentas, sin reglas de presentación. */
public interface RepositorioUsuario {
    boolean existeAdministrador() throws SQLException;
    CredencialesUsuario buscarCredenciales(String correo) throws SQLException;
    Usuario crearAdministradorInicial(String nombre, String correo, String hashContrasena)
            throws SQLException;
    Usuario crearCliente(String nombre, String correo, String hashContrasena)
            throws SQLException;
}
