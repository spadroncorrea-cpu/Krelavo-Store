package krelavo.store.modelo;

/** Indicadores calculados desde la base de datos. */
public final class ResumenAdministrativo {
    private final long productos, existencias, pedidosPendientes, clientes, ventasCop;
    public ResumenAdministrativo(long productos, long existencias, long pedidosPendientes,
            long clientes, long ventasCop) {
        this.productos = productos; this.existencias = existencias;
        this.pedidosPendientes = pedidosPendientes; this.clientes = clientes; this.ventasCop = ventasCop;
    }
    public long obtenerProductos() { return productos; }
    public long obtenerExistencias() { return existencias; }
    public long obtenerPedidosPendientes() { return pedidosPendientes; }
    public long obtenerClientes() { return clientes; }
    public long obtenerVentasCop() { return ventasCop; }
}
