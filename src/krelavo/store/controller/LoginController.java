package krelavo.store.controller;

import java.io.InputStream;
import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import krelavo.store.Main;


public class LoginController {
    private static final String EMAIL = "sofia@gmail.com";
    private static final String PASSWORD = "Krelavo2026!";
    private final Main app;
    private Button submitButton;

    public LoginController(Main app) { this.app = app; }

    public Parent createView() {
        HBox root = new HBox();
        root.getStyleClass().add("login-screen");
        root.setMinWidth(900);

        StackPane photoPanel = createPhotoPanel();
        StackPane formPanel = createFormPanel();
        photoPanel.prefWidthProperty().bind(root.widthProperty().multiply(0.55));
        formPanel.prefWidthProperty().bind(root.widthProperty().multiply(0.45));
        photoPanel.maxWidthProperty().bind(root.widthProperty().multiply(0.55));
        formPanel.maxWidthProperty().bind(root.widthProperty().multiply(0.45));
        photoPanel.prefHeightProperty().bind(root.heightProperty());
        formPanel.prefHeightProperty().bind(root.heightProperty());
        root.getChildren().addAll(photoPanel, formPanel);
        return root;
    }

    private StackPane createPhotoPanel() {
        StackPane panel = new StackPane();
        panel.getStyleClass().add("login-photo-panel");
        VBox composition = new VBox(30);
        composition.setPadding(new Insets(54, 54, 48, 54));
        composition.setAlignment(Pos.CENTER_LEFT);

        Label eyebrow = new Label("KRELAVO STORE");
        eyebrow.getStyleClass().add("photo-eyebrow");
        Label phrase = new Label("Tu belleza,\ntu esencia.");
        phrase.getStyleClass().add("photo-heading");
        Label description = new Label("Rituales de cuidado elegidos para acompañar tu esencia.");
        description.getStyleClass().add("photo-description");
        VBox copy = new VBox(10, eyebrow, phrase, description);

        HBox products = new HBox(14);
        products.setAlignment(Pos.CENTER_LEFT);
        products.getChildren().addAll(productPhoto("serum-facial.png", 188, 274),
                productPhoto("ampolla-centella.png", 160, 232),
                productPhoto("protector-solar.png", 150, 216));
        Label note = new Label("Una selección de nuestros esenciales de skincare");
        note.getStyleClass().add("photo-caption");
        composition.getChildren().addAll(copy, products, note);
        panel.getChildren().add(composition);
        return panel;
    }

    private StackPane productPhoto(String file, double width, double height) {
        StackPane holder = new StackPane();
        holder.getStyleClass().add("login-product-photo");
        holder.setPrefSize(width, height);
        holder.setMinSize(width, height);
        holder.setMaxSize(width, height);
        ImageView image = imageView("/krelavo/store/resources/images/" + file, width - 14, height - 14);
        if (image != null) holder.getChildren().add(image);
        return holder;
    }

    private StackPane createFormPanel() {
        StackPane panel = new StackPane();
        panel.getStyleClass().add("login-form-panel");
        VBox form = new VBox(0);
        form.setAlignment(Pos.CENTER_LEFT);
        form.setMaxWidth(390);
        form.setPadding(new Insets(46, 28, 46, 28));

        Label logo = new Label("KRELAVO STORE");
        logo.getStyleClass().add("brand-title");
        Label tagline = new Label("TU BELLEZA, TU ESENCIA");
        tagline.getStyleClass().add("brand-tagline");
        VBox brand = new VBox(5, logo, tagline);
        brand.setAlignment(Pos.CENTER_LEFT);

        Label heading = new Label("Bienvenida de nuevo");
        heading.getStyleClass().add("login-heading");
        Label subtitle = new Label("Ingresa a tu cuenta para continuar");
        subtitle.getStyleClass().add("login-subtitle");

        Label emailLabel = fieldLabel("Correo electrónico");
        TextField email = new TextField();
        email.setPromptText("Ingresa tu correo electrónico");
        email.getStyleClass().add("login-input");
        email.setMaxWidth(Double.MAX_VALUE);
        Label passLabel = fieldLabel("Contraseña");
        PasswordField pass = new PasswordField();
        pass.setPromptText("Ingresa tu contraseña");
        pass.getStyleClass().add("login-input");
        pass.setMaxWidth(Double.MAX_VALUE);
        Label message = new Label(" ");
        message.getStyleClass().add("login-message");
        message.setWrapText(true);
        message.setMinHeight(24);
        Button submit = new Button("Iniciar sesión");
        submitButton = submit;
        submit.getStyleClass().add("primary-button");
        submit.setMaxWidth(Double.MAX_VALUE);
        Label register = new Label("¿No tienes cuenta? Regístrate.");
        register.getStyleClass().add("register-prompt");
        register.setOnMouseClicked(e -> info("Registro", "El registro no forma parte de esta entrega."));

        VBox.setMargin(brand, new Insets(0, 0, 62, 0));
        VBox.setMargin(heading, new Insets(0, 0, 9, 0));
        VBox.setMargin(subtitle, new Insets(0, 0, 35, 0));
        VBox.setMargin(emailLabel, new Insets(0, 0, 8, 0));
        VBox.setMargin(email, new Insets(0, 0, 22, 0));
        VBox.setMargin(passLabel, new Insets(0, 0, 8, 0));
        VBox.setMargin(pass, new Insets(0, 0, 8, 0));
        VBox.setMargin(message, new Insets(0, 0, 8, 0));
        VBox.setMargin(submit, new Insets(0, 0, 13, 0));
        form.getChildren().addAll(brand, heading, subtitle, emailLabel, email,
                passLabel, pass, message, submit, register);

        submit.setOnAction(e -> authenticate(email.getText(), pass.getText(), message));
        email.setOnAction(e -> pass.requestFocus());
        pass.setOnAction(e -> authenticate(email.getText(), pass.getText(), message));
        panel.getChildren().add(form);
        return panel;
    }

    private ImageView imageView(String path, double width, double height) {
        InputStream stream = getClass().getResourceAsStream(path);
        if (stream == null) return null;
        try {
            Image image = new Image(stream);
            ImageView view = new ImageView(image);
            view.setPreserveRatio(true);
            view.setFitWidth(width);
            view.setFitHeight(height);
            return view;
        } finally {
            try { stream.close(); } catch (Exception ignored) { }
        }
    }

    private Label fieldLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("field-label");
        return label;
    }

    private void authenticate(String rawEmail, String rawPassword, Label message) {
        String email = rawEmail == null ? "" : rawEmail.trim();
        String password = rawPassword == null ? "" : rawPassword;
        if (email.isEmpty() || password.trim().isEmpty()) {
            error(message, "Completa el correo electrónico y la contraseña.");
            return;
        }
        if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            error(message, "Ingresa un correo electrónico válido.");
            return;
        }
        if (!EMAIL.equalsIgnoreCase(email) || !PASSWORD.equals(password)) {
            error(message, "Correo o contraseña incorrectos.");
            return;
        }
        message.setText("Inicio de sesión correcto.");
        message.getStyleClass().remove("message-error");
        if (!message.getStyleClass().contains("message-success")) message.getStyleClass().add("message-success");
        submitButton.setText("¡Inicio correcto!");
        PauseTransition navigation = new PauseTransition(Duration.millis(900));
        navigation.setOnFinished(event -> app.showCatalog());
        navigation.play();
    }

    private void error(Label label, String text) {
        label.setText(text);
        label.getStyleClass().remove("message-success");
        if (!label.getStyleClass().contains("message-error")) label.getStyleClass().add("message-error");
    }

    private void info(String title, String text) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(text);
        alert.showAndWait();
    }
}