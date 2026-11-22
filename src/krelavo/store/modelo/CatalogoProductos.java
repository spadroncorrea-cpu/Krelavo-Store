package krelavo.store.modelo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Catálogo inicial usado para sembrar los productos originales. */
public final class CatalogoProductos {
    public static final String FACIAL = "Cuidado facial";
    public static final String CAPILAR = "Cuidado capilar";
    public static final String SIN_MARCA = "Marca por confirmar";

    private CatalogoProductos() { }

    public static List<Producto> crearProductos() {
        List<Producto> productos = new ArrayList<Producto>();
        agregarProducto(productos, "Sérum facial", "Beauty of Joseon", 45000, FACIAL, "serum-facial.png");
        agregarProducto(productos, "Protector solar", "Beauty of Joseon", 55000, FACIAL, "protector-solar.png");
        agregarProducto(productos, "Ampolla de centella", "SKIN1004", 49000, FACIAL, "ampolla-centella.png");
        agregarProducto(productos, "Esencia facial", "COSRX", 52000, FACIAL, "esencia-facial.png");
        agregarProducto(productos, "Sérum capilar", "Mise en Scène", 39000, CAPILAR, "serum-capilar.png");
        agregarProducto(productos, "Shampoo reparador", "Kundal", 42000, CAPILAR, "shampoo-reparador.png");
        agregarProducto(productos, "Mascarilla capilar", "Elizavecca", 35000, CAPILAR, "mascarilla-capilar.png");
        agregarProducto(productos, "Tratamiento capilar", "Kundal", 60000, CAPILAR, "tratamiento-capilar.png");
        agregarProducto(productos, "Crema hidratante de savia de abedul", "Round lab", 75000, FACIAL, "round lab.png");
        agregarProducto(productos, "Sérum hidratante de ácido hialurónico", "Torriden", 68000, FACIAL, "torriden.png");
        agregarProducto(productos, "Tónico facial AHA BHA PHA", "Some By Mi", 65000, FACIAL, "some by mi.png");
        agregarProducto(productos, "Discos limpiadores para poros", "Medicube", 85000, FACIAL, "medicube.png");
        agregarProducto(productos, "Tratamiento capilar en ampollas", "Lador", 45000, CAPILAR, "lador.png");
        agregarProducto(productos, "Shampoo de romero para el cuero cabelludo", "Aromatica", 72000, CAPILAR, "aromatica.png");
        agregarProducto(productos, "Mascarilla capilar reparadora", "Tsubaki", 78000, CAPILAR, "tsubaki.png");
        agregarProducto(productos, "Tratamiento capilar hidratante", "&honey", 82000, CAPILAR, "&honey.png");
        return Collections.unmodifiableList(productos);
    }

    private static void agregarProducto(List<Producto> productos, String nombre, String marca,
            long precio, String categoria, String nombreImagen) {
        productos.add(new Producto(nombre, marca, precio, categoria,
                "/krelavo/store/resources/images/" + nombreImagen));
    }

   
}
