package krelavo.store.controlador;

import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.concurrent.Task;
import javafx.scene.layout.VBox;
import krelavo.store.Main;
import krelavo.store.modelo.Usuario;
import krelavo.store.servicio.EjecutorAcceso;
import krelavo.store.servicio.ServicioAutenticacion;
import krelavo.store.vista.ComponentesFormulario;
import krelavo.store.vista.VistaAcceso;

/** Presenta el acceso compartido para cuentas de cliente y administradoras. */
public final class ControladorInicioSesion {
    private final Main aplicacion;
    private final ServicioAutenticacion servicioAutenticacion;

    public ControladorInicioSesion(Main aplicacion, ServicioAutenticacion servicioAutenticacion) {
        this.aplicacion = aplicacion;
        this.servicioAutenticacion = servicioAutenticacion;
    }

    public Parent crearVista() {
        VBox formulario = new VBox(8);
        TextField correo = ComponentesFormulario.crearCampoTexto("Ingresa tu correo electrónico");
        PasswordField contrasena = ComponentesFormulario.crearCampoContrasena("Ingresa tu contraseña");
        Label mensaje = ComponentesFormulario.crearMensaje();
        Button boton = ComponentesFormulario.crearBotonPrincipal("Iniciar sesión");
        Label registro = ComponentesFormulario.crearEnlace("¿No tienes cuenta? Regístrate.");

        VBox.setMargin(correo, new Insets(0, 0, 12, 0));
        VBox.setMargin(contrasena, new Insets(0, 0, 4, 0));
        VBox.setMargin(mensaje, new Insets(0, 0, 4, 0));
        VBox.setMargin(boton, new Insets(0, 0, 8, 0));
        formulario.getChildren().addAll(
                ComponentesFormulario.crearEtiqueta("Correo electrónico"), correo,
                ComponentesFormulario.crearEtiqueta("Contraseña"), contrasena,
                mensaje, boton, registro);

        boton.setOnAction(evento -> iniciarSesion(
                correo.getText(), contrasena.getText(), mensaje, formulario, boton, contrasena));
        correo.setOnAction(evento -> contrasena.requestFocus());
        contrasena.setOnAction(evento -> iniciarSesion(
                correo.getText(), contrasena.getText(), mensaje, formulario, boton, contrasena));
        registro.setOnMouseClicked(evento -> aplicacion.mostrarRegistroCliente());
        return VistaAcceso.crear("Bienvenida de nuevo", "Ingresa a tu cuenta para continuar", formulario);
    }

    private void iniciarSesion(String correo, String contrasena, Label mensaje,
            VBox formulario, Button boton, PasswordField campoContrasena) {
        if (formulario.isDisabled()) {
            return;
        }
        formulario.setDisable(true);
        boton.setText("Validando…");
        mensaje.setText("Comprobando tus credenciales…");
        mensaje.getStyleClass().remove("message-error");
        Task<Usuario> tarea = EjecutorAcceso.crearTarea(
                () -> servicioAutenticacion.autenticar(correo, contrasena));
        tarea.setOnSucceeded(evento -> {
            formulario.setDisable(false);
            campoContrasena.clear();
            aplicacion.iniciarSesion(tarea.getValue());
        });
        tarea.setOnFailed(evento -> {
            formulario.setDisable(false);
            boton.setText("Iniciar sesión");
            Throwable error = tarea.getException();
            String texto = error instanceof IllegalArgumentException
                    ? error.getMessage() : "No se pudo iniciar sesión. Inténtalo de nuevo.";
            mostrarError(mensaje, texto);
        });
        EjecutorAcceso.ejecutar(tarea);
    }

    private void mostrarError(Label mensaje, String texto) {
        mensaje.setText(texto);
        mensaje.getStyleClass().remove("message-success");
        if (!mensaje.getStyleClass().contains("message-error")) {
            mensaje.getStyleClass().add("message-error");
        }
    }
}
