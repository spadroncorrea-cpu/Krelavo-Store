package krelavo.store.controlador;

import javafx.geometry.Insets;
import javafx.concurrent.Task;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import krelavo.store.Main;
import krelavo.store.servicio.EjecutorAcceso;
import krelavo.store.servicio.ServicioAutenticacion;
import krelavo.store.vista.ComponentesFormulario;
import krelavo.store.vista.VistaAcceso;

/** Configura una única cuenta administradora durante el primer inicio. */
public final class ControladorConfiguracionInicial {
    private final Main aplicacion;
    private final ServicioAutenticacion servicioAutenticacion;

    public ControladorConfiguracionInicial(Main aplicacion, ServicioAutenticacion servicioAutenticacion) {
        this.aplicacion = aplicacion;
        this.servicioAutenticacion = servicioAutenticacion;
    }

    public Parent crearVista() {
        VBox formulario = new VBox(6);
        TextField nombre = ComponentesFormulario.crearCampoTexto("Nombre de la persona administradora");
        TextField correo = ComponentesFormulario.crearCampoTexto("Correo electrónico");
        PasswordField contrasena = ComponentesFormulario.crearCampoContrasena("Mínimo 12 caracteres");
        PasswordField confirmacion = ComponentesFormulario.crearCampoContrasena("Confirma la contraseña");
        Label mensaje = ComponentesFormulario.crearMensaje();
        Button boton = ComponentesFormulario.crearBotonPrincipal("Crear cuenta administradora");

        VBox.setMargin(nombre, new Insets(0, 0, 3, 0));
        VBox.setMargin(correo, new Insets(0, 0, 3, 0));
        VBox.setMargin(contrasena, new Insets(0, 0, 3, 0));
        formulario.getChildren().addAll(
                ComponentesFormulario.crearEtiqueta("Nombre"), nombre,
                ComponentesFormulario.crearEtiqueta("Correo electrónico"), correo,
                ComponentesFormulario.crearEtiqueta("Contraseña"), contrasena,
                ComponentesFormulario.crearEtiqueta("Confirmar contraseña"), confirmacion,
                mensaje, boton);

        boton.setOnAction(evento -> crearAdministrador(nombre, correo,
                contrasena, confirmacion, mensaje, formulario, boton));
        confirmacion.setOnAction(evento -> crearAdministrador(nombre, correo,
                contrasena, confirmacion, mensaje, formulario, boton));
        return VistaAcceso.crear("Configuración inicial",
                "Crea la primera cuenta administradora de Krelavo Store", formulario);
    }

    private void crearAdministrador(TextField nombre, TextField correo,
            PasswordField contrasena, PasswordField confirmacion, Label mensaje,
            VBox formulario, Button boton) {
        if (formulario.isDisabled()) {
            return;
        }
        String nombreIngresado = nombre.getText();
        String correoIngresado = correo.getText();
        String claveIngresada = contrasena.getText();
        String confirmacionIngresada = confirmacion.getText();
        formulario.setDisable(true);
        boton.setText("Creando cuenta…");
        mensaje.setText("Protegiendo la contraseña y preparando la tienda…");
        mensaje.getStyleClass().remove("message-error");
        Task<Void> tarea = EjecutorAcceso.crearTarea(() -> {
            servicioAutenticacion.crearAdministradorInicial(nombreIngresado, correoIngresado,
                    claveIngresada, confirmacionIngresada);
            return null;
        });
        tarea.setOnSucceeded(evento -> {
            formulario.setDisable(false);
            contrasena.clear();
            confirmacion.clear();
            mostrarAviso("Administrador creado", "La configuración inicial terminó. Inicia sesión para continuar.");
            aplicacion.mostrarInicioSesion();
        });
        tarea.setOnFailed(evento -> {
            formulario.setDisable(false);
            boton.setText("Crear cuenta administradora");
            Throwable error = tarea.getException();
            String texto = error instanceof IllegalArgumentException || error instanceof IllegalStateException
                    ? error.getMessage()
                    : "No se pudo crear la cuenta administradora. Inténtalo de nuevo.";
            mostrarError(mensaje, texto);
        });
        EjecutorAcceso.ejecutar(tarea);
    }

    private void mostrarAviso(String titulo, String texto) {
        Alert aviso = new Alert(Alert.AlertType.INFORMATION);
        aviso.setTitle(titulo);
        aviso.setHeaderText(null);
        aviso.setContentText(texto);
        aviso.showAndWait();
    }

    private void mostrarError(Label mensaje, String texto) {
        mensaje.setText(texto);
        mensaje.getStyleClass().remove("message-success");
        if (!mensaje.getStyleClass().contains("message-error")) {
            mensaje.getStyleClass().add("message-error");
        }
    }
}
