package krelavo.store.modelo;

import java.text.NumberFormat;
import java.util.Locale;

/** Información de un producto visible en el catálogo. */
public class Producto {
    private final String nombre;
    private final String marca;
    private final long precioCop;
    private final String categoria;
    private final String rutaImagen;
    private final boolean pendiente;
    private final long identificador;
    private final int cantidadExistencias;
    private boolean favorito;

    public Producto(String nombre, String marca, long precioCop, String categoria, String rutaImagen) {
        this(nombre, marca, precioCop, categoria, rutaImagen, false, -1, 0);
    }

    public Producto(String nombre, String marca, long precioCop, String categoria,
            String rutaImagen, boolean pendiente) {
        this(nombre, marca, precioCop, categoria, rutaImagen, pendiente, -1, 0);
    }

    public Producto(long identificador, String nombre, String marca, long precioCop,
            String categoria, String rutaImagen, int cantidadExistencias) {
        this(nombre, marca, precioCop, categoria, rutaImagen, false,
                identificador, cantidadExistencias);
    }

    private Producto(String nombre, String marca, long precioCop, String categoria,
            String rutaImagen, boolean pendiente, long identificador, int cantidadExistencias) {
        this.nombre = nombre;
        this.marca = marca;
        this.precioCop = precioCop;
        this.categoria = categoria;
        this.rutaImagen = rutaImagen;
        this.pendiente = pendiente;
        this.identificador = identificador;
        this.cantidadExistencias = cantidadExistencias;
    }

    public Producto(String nombre, String marca, String precio, String categoria, String rutaImagen) {
        this(nombre, marca, convertirPrecio(precio), categoria, rutaImagen, false);
    }

    private static long convertirPrecio(String precio) {
        if (precio == null) return 0;
        String digitos = precio.replaceAll("[^0-9]", "");
        return digitos.isEmpty() ? 0 : Long.parseLong(digitos);
    }

    public String obtenerNombre() { return nombre; }
    public String obtenerMarca() { return marca; }
    public String obtenerPrecioFormateado() {
        if (pendiente || precioCop <= 0) return "Precio pendiente";
        NumberFormat formateador = NumberFormat.getInstance(new Locale("es", "CO"));
        formateador.setMaximumFractionDigits(0);
        return "$" + formateador.format(precioCop) + " COP";
    }
    public long obtenerPrecioCop() { return precioCop; }
    public String obtenerCategoria() { return categoria; }
    public String obtenerRutaImagen() { return rutaImagen; }
    public boolean estaPendiente() { return pendiente; }
    public long obtenerIdentificador() { return identificador; }
    public int obtenerCantidadExistencias() { return cantidadExistencias; }
    public boolean esFavorito() { return favorito; }
    public void establecerFavorito(boolean favorito) { this.favorito = favorito; }
}
