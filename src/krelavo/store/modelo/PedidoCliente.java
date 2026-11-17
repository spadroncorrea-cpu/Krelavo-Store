package krelavo.store.modelo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Pedido e instantánea de entrega visibles únicamente a su cliente. */
public final class PedidoCliente {
    private final long identificador;
    private final String codigo, fecha, estado, destinatario, telefono, linea, ciudad, departamento, indicaciones;
    private final long subtotal, envio, total;
    private final List<LineaCarrito> lineas;
    public PedidoCliente(long identificador,String codigo,String fecha,String estado,long subtotal,long envio,long total,
            String destinatario,String telefono,String linea,String ciudad,String departamento,String indicaciones,
            List<LineaCarrito> lineas){
        this.identificador=identificador;this.codigo=codigo;this.fecha=fecha;this.estado=estado;
        this.subtotal=subtotal;this.envio=envio;this.total=total;this.destinatario=destinatario;
        this.telefono=telefono;this.linea=linea;this.ciudad=ciudad;this.departamento=departamento;
        this.indicaciones=indicaciones==null?"":indicaciones;
        this.lineas=Collections.unmodifiableList(new ArrayList<LineaCarrito>(lineas));
    }
    public long obtenerIdentificador(){return identificador;} public String obtenerCodigo(){return codigo;}
    public String obtenerFecha(){return fecha;} public String obtenerEstado(){return estado;}
    public long obtenerSubtotal(){return subtotal;} public long obtenerEnvio(){return envio;} public long obtenerTotal(){return total;}
    public String obtenerDestinatario(){return destinatario;} public String obtenerTelefono(){return telefono;}
    public String obtenerLinea(){return linea;} public String obtenerCiudad(){return ciudad;}
    public String obtenerDepartamento(){return departamento;} public String obtenerIndicaciones(){return indicaciones;}
    public List<LineaCarrito> obtenerLineas(){return lineas;}
    public int obtenerCantidadProductos(){int total=0;for(LineaCarrito l:lineas)total+=l.obtenerCantidad();return total;}
}
