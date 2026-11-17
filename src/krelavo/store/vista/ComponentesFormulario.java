package krelavo.store.vista;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/** Crea controles de formulario con los estilos comunes de Krelavo Store. */
public final class ComponentesFormulario {
    private ComponentesFormulario() { }

    public static Label crearEtiqueta(String texto) {
        Label etiqueta = new Label(texto);
        etiqueta.getStyleClass().add("field-label");
        return etiqueta;
    }

    public static TextField crearCampoTexto(String indicacion) {
        TextField campo = new TextField();
        campo.setPromptText(indicacion);
        campo.getStyleClass().add("login-input");
        campo.setMaxWidth(Double.MAX_VALUE);
        return campo;
    }

    public static PasswordField crearCampoContrasena(String indicacion) {
        PasswordField campo = new PasswordField();
        campo.setPromptText(indicacion);
        campo.getStyleClass().add("login-input");
        campo.setMaxWidth(Double.MAX_VALUE);
        return campo;
    }

    public static Button crearBotonPrincipal(String texto) {
        Button boton = new Button(texto);
        boton.getStyleClass().add("primary-button");
        boton.setMaxWidth(Double.MAX_VALUE);
        return boton;
    }

    public static Label crearEnlace(String texto) {
        Label enlace = new Label(texto);
        enlace.getStyleClass().add("register-prompt");
        return enlace;
    }

    public static Label crearMensaje() {
        Label mensaje = new Label(" ");
        mensaje.getStyleClass().add("login-message");
        mensaje.setWrapText(true);
        mensaje.setMinHeight(24);
        return mensaje;
    }
}
