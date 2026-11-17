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

/** Permite el registro público de cuentas CLIENTE exclusivamente. */
public final class ControladorRegistroCliente {
    private final Main aplicacion;
    private final ServicioAutenticacion servicioAutenticacion;

    public ControladorRegistroCliente(Main aplicacion, ServicioAutenticacion servicioAutenticacion) {
        this.aplicacion = aplicacion;
        this.servicioAutenticacion = servicioAutenticacion;
    }

    public Parent crearVista() {
        VBox formulario = new VBox(7);
        TextField nombre = ComponentesFormulario.crearCampoTexto("Ingresa tu nombre");
        TextField correo = ComponentesFormulario.crearCampoTexto("Ingresa tu correo electrónico");
        PasswordField contrasena = ComponentesFormulario.crearCampoContrasena("Mínimo 12 caracteres");
        PasswordField confirmacion = ComponentesFormulario.crearCampoContrasena("Repite tu contraseña");
        Label mensaje = ComponentesFormulario.crearMensaje();
        Button boton = ComponentesFormulario.crearBotonPrincipal("Crear cuenta de cliente");
        Label enlace = ComponentesFormulario.crearEnlace("¿Ya tienes cuenta? Inicia sesión.");

        VBox.setMargin(nombre, new Insets(0, 0, 5, 0));
        VBox.setMargin(correo, new Insets(0, 0, 5, 0));
        VBox.setMargin(contrasena, new Insets(0, 0, 5, 0));
        VBox.setMargin(mensaje, new Insets(0, 0, 3, 0));
        formulario.getChildren().addAll(
                ComponentesFormulario.crearEtiqueta("Nombre"), nombre,
                ComponentesFormulario.crearEtiqueta("Correo electrónico"), correo,
                ComponentesFormulario.crearEtiqueta("Contraseña"), contrasena,
                ComponentesFormulario.crearEtiqueta("Confirmar contraseña"), confirmacion,
                mensaje, boton, enlace);

        boton.setOnAction(evento -> registrar(nombre, correo, contrasena,
                confirmacion, mensaje, formulario, boton));
        confirmacion.setOnAction(evento -> registrar(nombre, correo, contrasena,
                confirmacion, mensaje, formulario, boton));
        enlace.setOnMouseClicked(evento -> aplicacion.mostrarInicioSesion());
        return VistaAcceso.crear("Crear cuenta", "Únete a Krelavo Store para continuar", formulario);
    }

    private void registrar(TextField nombre, TextField correo, PasswordField contrasena,
            PasswordField confirmacion, Label mensaje, VBox formulario, Button boton) {
        if (formulario.isDisabled()) {
            return;
        }
        String nombreIngresado = nombre.getText();
        String correoIngresado = correo.getText();
        String claveIngresada = contrasena.getText();
        String confirmacionIngresada = confirmacion.getText();
        formulario.setDisable(true);
        boton.setText("Creando cuenta…");
        mensaje.setText("Protegiendo tu contraseña y creando la cuenta…");
        mensaje.getStyleClass().remove("message-error");
        Task<krelavo.store.modelo.Usuario> tarea = EjecutorAcceso.crearTarea(() ->
                servicioAutenticacion.registrarCliente(nombreIngresado, correoIngresado,
                        claveIngresada, confirmacionIngresada));
        tarea.setOnSucceeded(evento -> {
            formulario.setDisable(false);
            contrasena.clear();
            confirmacion.clear();
            mostrarAviso("Cuenta creada", "Tu cuenta de cliente está lista. Ahora puedes iniciar sesión.");
            aplicacion.mostrarInicioSesion();
        });
        tarea.setOnFailed(evento -> {
            formulario.setDisable(false);
            boton.setText("Crear cuenta de cliente");
            Throwable error = tarea.getException();
            String texto = error instanceof IllegalArgumentException
                    ? error.getMessage()
                    : "No se pudo crear la cuenta. Revisa los datos e inténtalo de nuevo.";
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
