package krelavo.store.servicio;

import java.sql.SQLException;
import java.util.Locale;
import krelavo.store.accesodatos.RepositorioUsuario;
import krelavo.store.modelo.CredencialesUsuario;
import krelavo.store.modelo.Usuario;
import krelavo.store.seguridad.HashContrasenas;

/** Reglas de validación, registro, configuración inicial y autenticación. */
public final class ServicioAutenticacion {
    private static final int LONGITUD_MINIMA_CONTRASENA = 12;
    private static final int LONGITUD_MAXIMA_CONTRASENA = 128;
    private final RepositorioUsuario usuarioDao;
    private final HashContrasenas hashContrasenas;

    public ServicioAutenticacion(RepositorioUsuario usuarioDao, HashContrasenas hashContrasenas) {
        if (usuarioDao == null || hashContrasenas == null) {
            throw new IllegalArgumentException("Se requieren el DAO de usuario y el hasher.");
        }
        this.usuarioDao = usuarioDao;
        this.hashContrasenas = hashContrasenas;
    }

    public boolean hayAdministrador() throws SQLException {
        return usuarioDao.existeAdministrador();
    }

    public Usuario crearAdministradorInicial(String nombre, String correo,
            String contrasena, String confirmacion) throws SQLException {
        validarCuenta(nombre, correo, contrasena, confirmacion);
        if (usuarioDao.existeAdministrador()) {
            throw new IllegalStateException("La configuración inicial ya fue completada.");
        }
        char[] clave = contrasena.toCharArray();
        try {
            return usuarioDao.crearAdministradorInicial(nombre.trim(), normalizarCorreo(correo),
                    hashContrasenas.generarHash(clave));
        } catch (SQLException ex) {
            if (esConflictoUnico(ex)) {
                throw new IllegalArgumentException("El correo ya está registrado o la configuración inicial ya fue completada.");
            }
            throw ex;
        } finally {
            HashContrasenas.limpiar(clave);
        }
    }

    public Usuario registrarCliente(String nombre, String correo,
            String contrasena, String confirmacion) throws SQLException {
        validarCuenta(nombre, correo, contrasena, confirmacion);
        char[] clave = contrasena.toCharArray();
        try {
            return usuarioDao.crearCliente(nombre.trim(), normalizarCorreo(correo),
                    hashContrasenas.generarHash(clave));
        } catch (SQLException ex) {
            if (esConflictoUnico(ex)) {
                throw new IllegalArgumentException("Ya existe una cuenta con ese correo electrónico.");
            }
            throw ex;
        } finally {
            HashContrasenas.limpiar(clave);
        }
    }

    public Usuario autenticar(String correo, String contrasena) throws SQLException {
        if (correo == null || correo.trim().isEmpty()
                || contrasena == null || contrasena.isEmpty()) {
            throw new IllegalArgumentException("Completa el correo electrónico y la contraseña.");
        }
        String correoNormalizado = normalizarCorreo(correo);
        if (!correoValido(correoNormalizado)) {
            throw new IllegalArgumentException("Correo o contraseña incorrectos.");
        }
        CredencialesUsuario credenciales = usuarioDao.buscarCredenciales(correoNormalizado);
        char[] clave = contrasena.toCharArray();
        try {
            if (credenciales == null || !credenciales.obtenerUsuario().estaActivo()
                    || !hashContrasenas.verificar(clave, credenciales.obtenerHashContrasena())) {
                throw new IllegalArgumentException("Correo o contraseña incorrectos.");
            }
            return credenciales.obtenerUsuario();
        } finally {
            HashContrasenas.limpiar(clave);
        }
    }

    private void validarCuenta(String nombre, String correo,
            String contrasena, String confirmacion) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("Ingresa tu nombre.");
        }
        if (nombre.trim().length() > 120) {
            throw new IllegalArgumentException("El nombre no puede superar 120 caracteres.");
        }
        String correoNormalizado = normalizarCorreo(correo);
        if (!correoValido(correoNormalizado)) {
            throw new IllegalArgumentException("Ingresa un correo electrónico válido.");
        }
        if (contrasena == null || contrasena.length() < LONGITUD_MINIMA_CONTRASENA
                || contrasena.length() > LONGITUD_MAXIMA_CONTRASENA) {
            throw new IllegalArgumentException("La contraseña debe tener entre 12 y 128 caracteres.");
        }
        if (!contrasena.equals(confirmacion)) {
            throw new IllegalArgumentException("La confirmación de contraseña no coincide.");
        }
    }

    private String normalizarCorreo(String correo) {
        return correo == null ? "" : correo.trim().toLowerCase(Locale.ROOT);
    }

    private boolean correoValido(String correo) {
        return correo.length() <= 254
                && correo.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    }

    private boolean esConflictoUnico(SQLException ex) {
        SQLException actual = ex;
        while (actual != null) {
            if (actual.getErrorCode() == 19) {
                return true;
            }
            actual = actual.getNextException();
        }
        return false;
    }
}
