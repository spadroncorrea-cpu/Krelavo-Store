package krelavo.store.vista;

import java.io.InputStream;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/** Contenedor visual compartido por Login, registro y configuración inicial. */
public final class VistaAcceso {
    private VistaAcceso() { }

    public static Parent crear(String titulo, String subtitulo, Node formulario) {
        HBox raiz = new HBox();
        raiz.getStyleClass().add("login-screen");
        raiz.setMinWidth(900);

        StackPane panelFotografico = crearPanelFotografico();
        StackPane panelFormulario = new StackPane();
        panelFormulario.getStyleClass().add("login-form-panel");
        VBox contenido = new VBox(0);
        contenido.setAlignment(Pos.CENTER_LEFT);
        contenido.setMaxWidth(390);
        contenido.setPadding(new Insets(38, 28, 38, 28));

        javafx.scene.control.Label marca = new javafx.scene.control.Label("KRELAVO STORE");
        marca.getStyleClass().add("brand-title");
        javafx.scene.control.Label lema = new javafx.scene.control.Label("TU BELLEZA, TU ESENCIA");
        lema.getStyleClass().add("brand-tagline");
        VBox identidad = new VBox(5, marca, lema);
        javafx.scene.control.Label encabezado = new javafx.scene.control.Label(titulo);
        encabezado.getStyleClass().add("login-heading");
        encabezado.setWrapText(true);
        javafx.scene.control.Label descripcion = new javafx.scene.control.Label(subtitulo);
        descripcion.getStyleClass().add("login-subtitle");
        descripcion.setWrapText(true);

        VBox.setMargin(identidad, new Insets(0, 0, 36, 0));
        VBox.setMargin(encabezado, new Insets(0, 0, 8, 0));
        VBox.setMargin(descripcion, new Insets(0, 0, 24, 0));
        contenido.getChildren().addAll(identidad, encabezado, descripcion, formulario);
        panelFormulario.getChildren().add(contenido);

        panelFotografico.prefWidthProperty().bind(raiz.widthProperty().multiply(0.55));
        panelFormulario.prefWidthProperty().bind(raiz.widthProperty().multiply(0.45));
        panelFotografico.maxWidthProperty().bind(raiz.widthProperty().multiply(0.55));
        panelFormulario.maxWidthProperty().bind(raiz.widthProperty().multiply(0.45));
        panelFotografico.prefHeightProperty().bind(raiz.heightProperty());
        panelFormulario.prefHeightProperty().bind(raiz.heightProperty());
        raiz.getChildren().addAll(panelFotografico, panelFormulario);
        return raiz;
    }

    private static StackPane crearPanelFotografico() {
        StackPane panel = new StackPane();
        panel.getStyleClass().add("login-photo-panel");
        VBox composicion = new VBox(30);
        composicion.setPadding(new Insets(54, 54, 48, 54));
        composicion.setAlignment(Pos.CENTER_LEFT);

        javafx.scene.control.Label marca = new javafx.scene.control.Label("KRELAVO STORE");
        marca.getStyleClass().add("photo-eyebrow");
        javafx.scene.control.Label lema = new javafx.scene.control.Label("Tu belleza,\ntu esencia.");
        lema.getStyleClass().add("photo-heading");
        javafx.scene.control.Label descripcion = new javafx.scene.control.Label(
                "Rituales de cuidado elegidos para acompañar tu esencia.");
        descripcion.getStyleClass().add("photo-description");
        VBox texto = new VBox(10, marca, lema, descripcion);

        HBox fotografias = new HBox(14);
        fotografias.setAlignment(Pos.CENTER_LEFT);
        fotografias.getChildren().addAll(
                crearFotografia("serum-facial.png", 188, 274),
                crearFotografia("ampolla-centella.png", 160, 232),
                crearFotografia("protector-solar.png", 150, 216));
        javafx.scene.control.Label nota = new javafx.scene.control.Label(
                "Una selección de nuestros esenciales de skincare");
        nota.getStyleClass().add("photo-caption");
        composicion.getChildren().addAll(texto, fotografias, nota);
        panel.getChildren().add(composicion);
        return panel;
    }

    private static StackPane crearFotografia(String archivo, double ancho, double alto) {
        StackPane contenedor = new StackPane();
        contenedor.getStyleClass().add("login-product-photo");
        contenedor.setPrefSize(ancho, alto);
        contenedor.setMinSize(ancho, alto);
        contenedor.setMaxSize(ancho, alto);
        ImageView vista = cargarImagen("/krelavo/store/resources/images/" + archivo,
                ancho - 14, alto - 14);
        if (vista != null) {
            contenedor.getChildren().add(vista);
        }
        return contenedor;
    }

    private static ImageView cargarImagen(String ruta, double ancho, double alto) {
        InputStream flujo = VistaAcceso.class.getResourceAsStream(ruta);
        if (flujo == null) {
            return null;
        }
        try {
            Image imagen = new Image(flujo);
            ImageView vista = new ImageView(imagen);
            vista.setPreserveRatio(true);
            vista.setFitWidth(ancho);
            vista.setFitHeight(alto);
            return vista;
        } finally {
            try { flujo.close(); } catch (Exception ignorado) { }
        }
    }
}
