package krelavo.store.model;

import java.text.NumberFormat;
import java.util.Locale;

public class Product {
    private final String name;
    private final String brand;
    private final long priceCop;
    private final String category;
    private final String image;
    private final boolean placeholder;
    private boolean favorite;

    public Product(String name, String brand, long priceCop, String category, String image) {
        this(name, brand, priceCop, category, image, false);
    }

    public Product(String name, String brand, long priceCop, String category, String image, boolean placeholder) {
        this.name = name;
        this.brand = brand;
        this.priceCop = priceCop;
        this.category = category;
        this.image = image;
        this.placeholder = placeholder;
    }

    public Product(String name, String brand, String price, String category, String image) {
        this(name, brand, parsePrice(price), category, image, false);
    }

    private static long parsePrice(String price) {
        if (price == null) return 0;
        String digits = price.replaceAll("[^0-9]", "");
        return digits.isEmpty() ? 0 : Long.parseLong(digits);
    }

    public String getName() { return name; }
    public String getBrand() { return brand; }
    public String getPrice() {
        if (placeholder || priceCop <= 0) return "Precio pendiente";
        NumberFormat format = NumberFormat.getInstance(new Locale("es", "CO"));
        format.setMaximumFractionDigits(0);
        return "$" + format.format(priceCop) + " COP";
    }
    public long getPriceCop() { return priceCop; }
    public String getCategory() { return category; }
    public String getImage() { return image; }
    public boolean isPlaceholder() { return placeholder; }
    public boolean isFavorite() { return favorite; }
    public void setFavorite(boolean value) { favorite = value; }
}
