package krelavo.store.modelo;

/** Datos de perfil y permisos de una cuenta; no contiene credenciales. */
public final class Usuario {
    private final long identificador;
    private final String nombre;
    private final String correo;
    private final Rol rol;
    private final boolean activo;
    private final String telefono;
    private final String fechaRegistro;

    public Usuario(long identificador, String nombre, String correo, Rol rol, boolean activo) {
        this(identificador, nombre, correo, rol, activo, "", "");
    }

    public Usuario(long identificador, String nombre, String correo, Rol rol, boolean activo,
            String telefono, String fechaRegistro) {
        this.identificador = identificador;
        this.nombre = nombre;
        this.correo = correo;
        this.rol = rol;
        this.activo = activo;
        this.telefono = telefono == null ? "" : telefono;
        this.fechaRegistro = fechaRegistro == null ? "" : fechaRegistro;
    }

    public long obtenerIdentificador() { return identificador; }
    public String obtenerNombre() { return nombre; }
    public String obtenerCorreo() { return correo; }
    public Rol obtenerRol() { return rol; }
    public boolean estaActivo() { return activo; }
    public String obtenerTelefono() { return telefono; }
    public String obtenerFechaRegistro() { return fechaRegistro; }
}
