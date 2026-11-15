package krelavo.store.controller;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Line;
import javafx.util.Duration;
import krelavo.store.Main;
import krelavo.store.model.Product;
import krelavo.store.model.ProductCatalog;

/** Catálogo cosmético con búsqueda, filtros, favoritos y contador local del carrito. */
public class CatalogController {
    private static final String FACIAL = ProductCatalog.FACIAL;
    private static final String CAPILAR = ProductCatalog.CAPILAR;
    private static final String ALL = "Todas las categorías";
    private static final String ALL_BRANDS = "Todas las marcas";
    private final Main app;
    private final List<Product> products = ProductCatalog.createProducts();
    private final List<Card> cards = new ArrayList<Card>();
    private ScrollPane scroll;
    private VBox content, featured, hair;
    private GridPane facialGrid, hairGrid;
    private TextField search;
    private ComboBox<String> categoryFilter, brandFilter, priceFilter;
    private Button favoriteNav, cartButton;
    private Label skinEmpty, hairEmpty;
    private boolean onlyFavorites;
    private int cartItemCount;

    public CatalogController(Main app) { this.app = app; }

    public Parent createView() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("catalog-screen");
        root.setMinWidth(1024);
        content = new VBox(0);
        content.getStyleClass().add("catalog-content");
        content.setMinWidth(1024);
        content.setPrefWidth(1400);
        content.setMaxWidth(Double.MAX_VALUE);
        content.getChildren().addAll(promoBar(), header(), divider(), welcomeBanner(),
                searchSection(), categorySection(), brandSection(), featuredSection(), hairSection(), footer());
        scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setPannable(true);
        scroll.getStyleClass().add("catalog-scroll");
        root.setCenter(scroll);
        return root;
    }

    private HBox promoBar() {
        HBox bar = new HBox();
        bar.setAlignment(Pos.CENTER);
        bar.setMinHeight(32);
        bar.setPrefHeight(32);
        bar.getStyleClass().add("promo-bar");
        Label message = new Label("Descubre tus esenciales para cada ritual de belleza");
        message.getStyleClass().add("promo-message");
        bar.getChildren().add(message);
        return bar;
    }

    private VBox header() {
        VBox header = new VBox();
        header.getStyleClass().add("catalog-header");
        HBox row = new HBox(24);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(18, 70, 18, 70));

        VBox brand = new VBox(3);
        Label name = new Label("KRELAVO STORE");
        name.getStyleClass().add("catalog-brand-title");
        Label tagline = new Label("TU BELLEZA, TU ESENCIA");
        tagline.getStyleClass().add("catalog-brand-tagline");
        brand.getChildren().addAll(name, tagline);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox nav = new HBox(18);
        nav.setAlignment(Pos.CENTER_RIGHT);
        Button home = navButton("Inicio");
        Button product = navButton("Productos");
        favoriteNav = navButton("Favoritos");
        cartButton = navButton("Carrito · 0");
        Button login = navButton("Iniciar sesión");
        Button register = navButton("Crear cuenta");
        home.setOnAction(e -> scrollTo(0));
        product.setOnAction(e -> scrollToSection(featured));
        favoriteNav.setOnAction(e -> {
            onlyFavorites = !onlyFavorites;
            active(favoriteNav, onlyFavorites);
            applyFilters();
            scrollToSection(featured);
        });
        cartButton.setOnAction(e -> info("Carrito", cartItemCount == 0
                ? "Aún no has agregado productos al carrito."
                : "Productos agregados en esta sesión: " + cartItemCount + "."));
        login.setOnAction(e -> app.showLogin());
        register.setOnAction(e -> info("Crear cuenta", "El registro no forma parte de este avance."));
        nav.getChildren().addAll(home, product, favoriteNav, cartButton, login, register);
        row.getChildren().addAll(brand, spacer, nav);
        header.getChildren().add(row);
        return header;
    }

    private Line divider() {
        Line line = new Line(0, 0, 1400, 0);
        line.getStyleClass().add("header-divider");
        line.endXProperty().bind(content.widthProperty());
        return line;
    }

    private VBox welcomeBanner() {
        VBox banner = new VBox(7);
        banner.setPadding(new Insets(30, 76, 24, 76));
        banner.getStyleClass().add("welcome-banner");
        Label eyebrow = new Label("KRELAVO · CUIDADO PERSONAL");
        eyebrow.getStyleClass().add("banner-eyebrow");
        Label title = new Label("Tu ritual, a tu manera.");
        title.getStyleClass().add("banner-title");
        Label body = new Label("Encuentra productos para cuidar tu piel y tu cabello.");
        body.getStyleClass().add("banner-copy");
        banner.getChildren().addAll(eyebrow, title, body);
        return banner;
    }

    private VBox searchSection() {
        VBox section = new VBox(12);
        section.setPadding(new Insets(24, 76, 0, 76));
        Label title = sectionTitle("Encuentra tus favoritos");
        search = new TextField();
        search.setPromptText("Buscar por producto, marca o precio...");
        search.getStyleClass().add("search-input");
        search.setMaxWidth(Double.MAX_VALUE);
        search.textProperty().addListener((observable, oldValue, newValue) -> applyFilters());

        HBox filters = new HBox(10);
        filters.setAlignment(Pos.CENTER_LEFT);
        categoryFilter = combo(ALL, Arrays.asList(ALL, FACIAL, CAPILAR));
        categoryFilter.setId("category-filter");
        List<String> brands = new ArrayList<String>();
        brands.add(ALL_BRANDS);

        for (Product product : products) {
        if (!product.isPlaceholder() && !brands.contains(product.getBrand())) {
        brands.add(product.getBrand());
    }
}

        brandFilter = combo(ALL_BRANDS, brands);
        brandFilter.setId("brand-filter");
        priceFilter = combo("Todos los precios", Arrays.asList("Todos los precios", "Hasta $40.000",
                "$40.001 – $55.000", "Más de $55.000"));
        priceFilter.setId("price-filter");
        categoryFilter.setOnAction(e -> { clearFavoriteMode(); applyFilters(); });
        brandFilter.setOnAction(e -> { clearFavoriteMode(); applyFilters(); });
        priceFilter.setOnAction(e -> { clearFavoriteMode(); applyFilters(); });
        filters.getChildren().addAll(categoryFilter, brandFilter, priceFilter);
        section.getChildren().addAll(title, search, filters);
        return section;
    }

    private ComboBox<String> combo(String prompt, List<String> values) {
        ComboBox<String> combo = new ComboBox<String>();
        combo.getItems().addAll(values);
        combo.setValue(prompt);
        combo.getStyleClass().add("filter-select");
        combo.setPrefWidth(225);
        combo.setMaxWidth(260);
        return combo;
    }

    private VBox categorySection() {
        VBox section = sectionShell();
        Label title = sectionTitle("Compra por categoría");
        HBox row = new HBox(18);
        row.getChildren().addAll(categoryCard("Cuidado facial", "Esenciales para tu rutina facial", FACIAL),
                categoryCard("Cuidado capilar", "Productos para tu rutina capilar", CAPILAR));
        HBox.setHgrow(row.getChildren().get(0), Priority.ALWAYS);
        HBox.setHgrow(row.getChildren().get(1), Priority.ALWAYS);
        section.getChildren().addAll(title, row);
        return section;
    }

    private VBox categoryCard(String title, String subtitle, final String value) {
        VBox card = new VBox(7);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(19, 24, 19, 24));
        card.setPrefHeight(92);
        card.setMaxWidth(Double.MAX_VALUE);
        card.getStyleClass().add("category-card");
        Label heading = new Label(title);
        heading.getStyleClass().add("category-title");
        Label detail = new Label(subtitle);
        detail.getStyleClass().add("category-subtitle");
        card.getChildren().addAll(heading, detail);
        card.setCursor(Cursor.HAND);
        card.setOnMouseClicked(e -> {
            clearFavoriteMode();
            categoryFilter.setValue(value);
            brandFilter.setValue(ALL_BRANDS);
            priceFilter.setValue("Todos los precios");
            search.clear();
            applyFilters();
            scrollToSection(value.equals(CAPILAR) ? hair : featured);
        });
        return card;
    }

    private VBox brandSection() {
    VBox section = sectionShell();

    Label title = sectionTitle("Buscar por marca");

    Label subtitle = new Label(
        "Descubre tus marcas favoritas de cuidado facial y capilar"
    );
    subtitle.getStyleClass().add("section-subtitle");

    GridPane grid = new GridPane();
    grid.setHgap(14);
    grid.setVgap(14);
    grid.setMaxWidth(Double.MAX_VALUE);

    int column = 0;
    int row = 0;

    for (Product product : products) {
        if (product.isPlaceholder()) {
            continue;
        }

        String brandName = product.getBrand();
        boolean exists = false;

        for (javafx.scene.Node node : grid.getChildren()) {
            VBox existingCard = (VBox) node;
            Label existingTitle = (Label) existingCard.getChildren().get(0);

            if (existingTitle.getText().equals(brandName)) {
                exists = true;
                break;
            }
        }

        if (!exists) {
            VBox card = brandCard(brandName, "Explora sus productos");
            grid.add(card, column, row);

            column++;

            if (column == 4) {
                column = 0;
                row++;
            }
        }
    }

    for (int i = 0; i < 4; i++) {
        javafx.scene.layout.ColumnConstraints constraints =
            new javafx.scene.layout.ColumnConstraints();

        constraints.setPercentWidth(25);
        constraints.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().add(constraints);
    }

    section.getChildren().addAll(title, subtitle, grid);

    return section;
}

    private VBox brandCard(final String brandName, String descriptor) {
        VBox card = new VBox(6);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(17, 18, 17, 18));
        card.setPrefHeight(78);
        card.setMaxWidth(Double.MAX_VALUE);
        card.getStyleClass().add("brand-card");
        Label title = new Label(brandName);
        title.getStyleClass().add("brand-card-title");
        Label detail = new Label(descriptor);
        detail.getStyleClass().add("brand-card-description");
        card.getChildren().addAll(title, detail);
        card.setCursor(Cursor.HAND);
        card.setOnMouseClicked(e -> {
            clearFavoriteMode();
            categoryFilter.setValue(ALL);
            priceFilter.setValue("Todos los precios");
            brandFilter.setValue(brandName);
            search.clear();
            applyFilters();
            scrollToSection(featured);
        });
        return card;
    }

    private VBox featuredSection() {
    featured = sectionShell();

    Label title = sectionTitle("Productos destacados");
    Label subtitle = new Label("Cuidado facial");
    subtitle.getStyleClass().add("section-subtitle");

    facialGrid = productGrid(FACIAL);
    skinEmpty = emptyMessage();

    featured.getChildren().addAll(title, subtitle, facialGrid, skinEmpty);

    return featured;
}

    private VBox hairSection() {
        hair = sectionShell();
        Label title = sectionTitle("Cuidado capilar");
        Label subtitle = new Label("Esenciales para tu rutina de cabello");
        subtitle.getStyleClass().add("section-subtitle");
        
        hairGrid = productGrid(CAPILAR);
        hairEmpty = emptyMessage();
        
        hair.getChildren().addAll(title, subtitle, hairGrid, hairEmpty);
        return hair;
    }

    private GridPane productGrid(String type) {
        GridPane grid = new GridPane();
        grid.setHgap(18);
        grid.setVgap(18);
        grid.setMaxWidth(Double.MAX_VALUE);
        int column = 0;
        int row = 0;
        for (Product product : products) {
            if (!type.equals(product.getCategory())) continue;
            Card card = new Card(product);
            cards.add(card);
            grid.add(card.root, column, row);
            column++;
            if (column == 4) { column = 0; row++; }
        }
        for (int i = 0; i < 4; i++) {
            javafx.scene.layout.ColumnConstraints constraints = new javafx.scene.layout.ColumnConstraints();
            constraints.setPercentWidth(25);
            constraints.setHgrow(Priority.ALWAYS);
            grid.getColumnConstraints().add(constraints);
        }
        return grid;
    }

    private VBox sectionShell() {
        VBox section = new VBox(12);
        section.setPadding(new Insets(34, 76, 0, 76));
        section.getStyleClass().add("catalog-section");
        return section;
    }

    private Label sectionTitle(String text) {
        Label title = new Label(text);
        title.getStyleClass().add("section-title");
        return title;
    }

    private Label emptyMessage() {
        Label label = new Label("No hay productos que coincidan. Las fichas pendientes necesitan datos e imagen.");
        label.getStyleClass().add("empty-message");
        label.setVisible(false);
        label.setManaged(false);
        return label;
    }

    private HBox footer() {
        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setPadding(new Insets(26, 76, 28, 76));
        footer.getStyleClass().add("catalog-footer");
        Label brand = new Label("KRELAVO STORE  ·  Tu belleza, tu esencia");
        brand.getStyleClass().add("footer-brand");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label line = new Label("Cosmética y cuidado personal");
        line.getStyleClass().add("footer-copy");
        footer.getChildren().addAll(brand, spacer, line);
        return footer;
    }

    private Button navButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("nav-button");
        return button;
    }

    private void active(Button button, boolean on) {
        button.getStyleClass().remove("nav-active");
        if (on) button.getStyleClass().add("nav-active");
    }

    private void clearFavoriteMode() {
        onlyFavorites = false;
        if (favoriteNav != null) active(favoriteNav, false);
    }

    private void applyFilters() {
        if (search == null) return;
        String query = search.getText() == null ? "" : search.getText().trim().toLowerCase();
        String selectedCategory = categoryFilter.getValue();
        String selectedBrand = brandFilter.getValue();
        String selectedPrice = priceFilter.getValue();
        int facialCount = 0;
        int hairCount = 0;
        for (Card card : cards) {
            Product product = card.product;
            String searchable = (product.getName() + " " + product.getBrand() + " "
                    + product.getCategory() + " " + product.getPrice()).toLowerCase();
            boolean textMatch = query.isEmpty() || searchable.contains(query);
            boolean categoryMatch = ALL.equals(selectedCategory) || product.getCategory().equals(selectedCategory);
            boolean brandMatch = ALL_BRANDS.equals(selectedBrand) || product.getBrand().equals(selectedBrand);
            boolean priceMatch = matchesPrice(product, selectedPrice);
            boolean visible = textMatch && categoryMatch && brandMatch && priceMatch
                    && (!onlyFavorites || product.isFavorite());
            card.root.setVisible(visible);
            card.root.setManaged(visible);
            if (visible && FACIAL.equals(product.getCategory())) facialCount++;
            if (visible && CAPILAR.equals(product.getCategory())) hairCount++;
        }
        showEmpty(skinEmpty, facialCount == 0);
        showEmpty(hairEmpty, hairCount == 0);
        
        rearrangeGrid(facialGrid, FACIAL);
        rearrangeGrid(hairGrid, CAPILAR);
    }
    
    private void rearrangeGrid(GridPane grid, String category) {
    grid.getChildren().clear();

    int column = 0;
    int row = 0;

    for (Card card : cards) {
        if (category.equals(card.product.getCategory())
                && card.root.isVisible()) {

            card.root.setManaged(true);
            grid.add(card.root, column, row);

            column++;

            if (column == 4) {
                column = 0;
                row++;
            }
        }
    }
}

    private boolean matchesPrice(Product product, String selected) {
        if (selected == null || "Todos los precios".equals(selected)) return true;
        if (product.isPlaceholder()) return false;
        long price = product.getPriceCop();
        if ("Hasta $40.000".equals(selected)) return price <= 40000;
        if ("$40.001 – $55.000".equals(selected)) return price >= 40001 && price <= 55000;
        if ("Más de $55.000".equals(selected)) return price > 55000;
        return true;
    }

    private void showEmpty(Label label, boolean show) {
        label.setVisible(show);
        label.setManaged(show);
    }

    private void scrollTo(double value) {
        if (scroll != null) {
            scroll.setVvalue(value);
            scroll.setHvalue(0);
        }
    }

    private void scrollToSection(final VBox section) {
        if (section == null || scroll == null) return;
        Platform.runLater(() -> {
            double total = content.getBoundsInLocal().getHeight();
            double view = scroll.getViewportBounds().getHeight();
            if (total > view) {
                double y = section.getBoundsInParent().getMinY();
                scroll.setVvalue(Math.max(0, Math.min(1, y / (total - view))));
            }
        });
    }

    private void info(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private final class Card {
        private final Product product;
        private final VBox root = new VBox(0);

        Card(Product product) {
            this.product = product;
            root.setPrefSize(282, 370);
            root.setMinSize(0, 370);
            root.setMaxWidth(Double.MAX_VALUE);
            root.getStyleClass().add("product-card");

            StackPane photo = new StackPane();
            photo.setPrefHeight(228);
            photo.setMinHeight(228);
            photo.setMaxHeight(228);
            photo.getStyleClass().add("product-photo");
            if (product.isPlaceholder()) {
                VBox pending = new VBox(7);
                pending.setAlignment(Pos.CENTER);
                Label mark = new Label("＋");
                mark.getStyleClass().add("pending-mark");
                Label pendingTitle = new Label("Fotografía pendiente");
                pendingTitle.getStyleClass().add("pending-title");
                Label pendingDetail = new Label("Agregar archivo PNG o JPG");
                pendingDetail.getStyleClass().add("pending-detail");
                pending.getChildren().addAll(mark, pendingTitle, pendingDetail);
                photo.getChildren().add(pending);
                root.getStyleClass().add("product-pending");
            } else {
                Image image = loadImage(product.getImage());
                ImageView view = new ImageView(image);
                view.setPreserveRatio(true);
                view.setFitWidth(242);
                view.setFitHeight(208);
                view.getStyleClass().add("product-image");
                photo.getChildren().add(view);
            }

            Button heart = new Button(product.isFavorite() ? "♥" : "♡");
            heart.getStyleClass().add("favorite-button");
            StackPane.setAlignment(heart, Pos.TOP_RIGHT);
            StackPane.setMargin(heart, new Insets(10, 10, 0, 0));
            heart.setTooltip(new Tooltip("Agregar a favoritos"));
            heart.setOnAction(e -> {
                product.setFavorite(!product.isFavorite());
                heart.setText(product.isFavorite() ? "♥" : "♡");
                heart.getStyleClass().remove("favorite-selected");
                if (product.isFavorite()) heart.getStyleClass().add("favorite-selected");
                heart.setTooltip(new Tooltip(product.isFavorite() ? "Quitar de favoritos" : "Agregar a favoritos"));
                applyFilters();
            });
            photo.getChildren().add(heart);

            VBox details = new VBox(5);
            details.setPadding(new Insets(13, 16, 16, 16));
            Label name = new Label(product.getName());
            name.getStyleClass().add("product-name");
            name.setWrapText(true);
            name.setMinHeight(36);
            Label brand = new Label(product.getBrand());
            brand.getStyleClass().add("product-brand");
            Label price = new Label(product.getPrice());
            price.getStyleClass().add("product-price");
            Button add = new Button(product.isPlaceholder() ? "Foto y datos pendientes" : "Agregar al carrito");
            add.getStyleClass().add("product-add-button");
            add.setMaxWidth(Double.MAX_VALUE);
            add.setPrefHeight(36);
            add.setDisable(product.isPlaceholder());
            if (!product.isPlaceholder()) {
                add.setOnAction(e -> {
                    cartItemCount++;
                    cartButton.setText("Carrito · " + cartItemCount);
                    add.setText("Agregado ✓");
                    add.setTooltip(new Tooltip(product.getName() + " agregado. Artículos añadidos: " + cartItemCount));
                    PauseTransition reset = new PauseTransition(Duration.millis(1800));
                    reset.setOnFinished(done -> add.setText("Agregar al carrito"));
                    reset.play();
                });
            }
            details.getChildren().addAll(name, brand, price, add);
            root.getChildren().addAll(photo, details);
        }

        private Image loadImage(String path) {
            InputStream stream = getClass().getResourceAsStream(path);
            if (stream == null) throw new IllegalStateException("Falta la imagen original: " + path);
            try {
                Image image = new Image(stream);
                if (image.isError() || image.getWidth() <= 0 || image.getHeight() <= 0) {
                    throw new IllegalStateException("Imagen inválida: " + path);
                }
                return image;
            } finally {
                try { stream.close(); } catch (Exception ignored) { }
            }
        }
    }
}
