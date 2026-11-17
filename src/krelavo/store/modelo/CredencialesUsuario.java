package krelavo.store.modelo;

/** Resultado interno de consulta que asocia un perfil con su hash de contraseña. */
public final class CredencialesUsuario {
    private final Usuario usuario;
    private final String hashContrasena;

    public CredencialesUsuario(Usuario usuario, String hashContrasena) {
        this.usuario = usuario;
        this.hashContrasena = hashContrasena;
    }

    public Usuario obtenerUsuario() { return usuario; }
    public String obtenerHashContrasena() { return hashContrasena; }
}
