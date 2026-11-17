package krelavo.store.modelo;

/** Producto y cantidad persistidos en el carrito de un cliente. */
public final class LineaCarrito {
    private final Producto producto;
    private final int cantidad;
    public LineaCarrito(Producto producto,int cantidad){this.producto=producto;this.cantidad=cantidad;}
    public Producto obtenerProducto(){return producto;}
    public int obtenerCantidad(){return cantidad;}
    public long obtenerSubtotal(){return producto.obtenerPrecioCop()*cantidad;}
}
