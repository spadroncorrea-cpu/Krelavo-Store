package krelavo.store.seguridad;

import krelavo.store.modelo.Rol;
import krelavo.store.modelo.Usuario;

/** Mantiene la identidad autenticada durante la ejecución de la aplicación. */
public final class SesionUsuario {
    private Usuario usuarioActual;

    public void iniciar(Usuario usuario) {
        if (usuario == null || !usuario.estaActivo()) {
            throw new IllegalArgumentException("No se puede iniciar una sesión con esta cuenta.");
        }
        usuarioActual = usuario;
    }

    public void cerrar() {
        usuarioActual = null;
    }

    public boolean estaAutenticada() {
        return usuarioActual != null;
    }

    public boolean tieneRol(Rol rolRequerido) {
        return usuarioActual != null && usuarioActual.obtenerRol() == rolRequerido;
    }

    public Usuario obtenerUsuarioActual() {
        return usuarioActual;
    }
}
