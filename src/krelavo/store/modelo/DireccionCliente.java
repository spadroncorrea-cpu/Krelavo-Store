package krelavo.store.modelo;

/** Dirección de entrega perteneciente a una cuenta cliente. */
public final class DireccionCliente {
    private final long identificador;
    private final String destinatario, telefono, linea, ciudad, departamento, indicaciones;
    private final boolean principal;
    public DireccionCliente(long identificador,String destinatario,String telefono,String linea,
            String ciudad,String departamento,String indicaciones,boolean principal) {
        this.identificador=identificador;this.destinatario=destinatario;this.telefono=telefono;
        this.linea=linea;this.ciudad=ciudad;this.departamento=departamento;
        this.indicaciones=indicaciones==null?"":indicaciones;this.principal=principal;
    }
    public long obtenerIdentificador(){return identificador;}
    public String obtenerDestinatario(){return destinatario;}
    public String obtenerTelefono(){return telefono;}
    public String obtenerLinea(){return linea;}
    public String obtenerCiudad(){return ciudad;}
    public String obtenerDepartamento(){return departamento;}
    public String obtenerIndicaciones(){return indicaciones;}
    public boolean esPrincipal(){return principal;}
    @Override public String toString(){return (principal?"★ ":"")+destinatario+" · "+linea+", "+ciudad+", "+departamento;}
}
