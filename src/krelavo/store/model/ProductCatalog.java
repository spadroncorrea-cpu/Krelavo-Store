package krelavo.store.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Lista editable del catálogo. Cambia aquí nombre, marca, precio y ruta de imagen.
 * Los ocho registros incompletos quedan marcados para que no se confundan con productos reales.
 */
public final class ProductCatalog {
    public static final String FACIAL = "Cuidado facial";
    public static final String CAPILAR = "Cuidado capilar";
    public static final String SIN_MARCA = "Marca por confirmar";

    private ProductCatalog() { }

    public static List<Product> createProducts() {
        List<Product> products = new ArrayList<Product>();
        add(products, "Sérum facial", "Beauty of Joseon", 45000, FACIAL, "serum-facial.png");
        add(products, "Protector solar", "Beauty of Joseon", 55000, FACIAL, "protector-solar.png");
        add(products, "Ampolla de centella", "SKIN1004", 49000, FACIAL, "ampolla-centella.png");
        add(products, "Esencia facial", "COSRX", 52000, FACIAL, "esencia-facial.png");
        add(products, "Sérum capilar", "Mise en Scène", 39000, CAPILAR, "serum-capilar.png");
        add(products, "Shampoo reparador", "Kundal", 42000, CAPILAR, "shampoo-reparador.png");
        add(products, "Mascarilla capilar", "Elizavecca", 35000, CAPILAR, "mascarilla-capilar.png");
        add(products, "Tratamiento capilar", "Kundal", 60000, CAPILAR, "tratamiento-capilar.png");
        add(products, "Crema hidratante de savia de abedul","Round lab",75000 , FACIAL, "round lab.png");
        add(products, "Sérum hidratante de ácido hialurónico", "Torriden", 68000, FACIAL, "torriden.png");
        add(products, "Tónico facial AHA BHA PHA", "Some By Mi", 65000, FACIAL, "some by mi.png");
        add(products, "Discos limpiadores para poros", "Medicube", 85000, FACIAL,"medicube.png");
        add(products, "Tratamiento capilar en ampollas", "Lador", 45000, CAPILAR, "lador.png");
        add(products, "Shampoo de romero para el cuero cabelludo", "Aromatica", 72000, CAPILAR, "aromatica.png");
        add(products, "Mascarilla capilar reparadora", "Tsubaki", 78000, CAPILAR, "tsubaki.png");
        add(products, "Tratamiento capilar hidratante", "&honey", 82000, CAPILAR,"&honey.png");
   
        return Collections.unmodifiableList(products);
    }

    private static void add(List<Product> products, String name, String brand, long price,
            String category, String imageName) {
        products.add(new Product(name, brand, price, category,
                "/krelavo/store/resources/images/" + imageName));
    }

    private static void addPending(List<Product> products, int number, String category) {
        String section = FACIAL.equals(category) ? "facial" : "capilar";
        String image = "/krelavo/store/resources/images/pendientes/" + section + "-" + number + ".png";
        products.add(new Product("Espacio " + section + " " + number, SIN_MARCA,
                0, category, image, true));
    }
}
