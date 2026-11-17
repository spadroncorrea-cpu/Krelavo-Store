package krelavo.store.basedatos;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/** Crea conexiones SQLite y activa las opciones necesarias en cada una. */
public final class FabricaConexiones {
    private static final String PROPIEDAD_RUTA_BASE_DATOS = "krelavo.db.path";
    private final Path rutaBaseDatos;

    public FabricaConexiones() {
        this(rutaPredeterminadaBaseDatos());
    }

    public FabricaConexiones(Path rutaBaseDatos) {
        if (rutaBaseDatos == null) {
            throw new IllegalArgumentException("La ruta de la base de datos es obligatoria.");
        }
        this.rutaBaseDatos = rutaBaseDatos.toAbsolutePath().normalize();
    }

    public Path obtenerRutaBaseDatos() {
        return rutaBaseDatos;
    }

    public Connection abrirConexion() throws SQLException {
        Path directorioPadre = rutaBaseDatos.getParent();
        try {
            if (directorioPadre != null) {
                Files.createDirectories(directorioPadre);
            }
        } catch (IOException ex) {
            throw new SQLException("No se pudo crear el directorio persistente de Krelavo Store.", ex);
        }

        Connection conexion = DriverManager.getConnection("jdbc:sqlite:" + rutaBaseDatos.toString());
        boolean preparada = false;
        try (Statement sentencia = conexion.createStatement()) {
            sentencia.execute("PRAGMA foreign_keys = ON");
            sentencia.execute("PRAGMA busy_timeout = 5000");
            preparada = true;
            return conexion;
        } finally {
            if (!preparada) {
                conexion.close();
            }
        }
    }

    private static Path rutaPredeterminadaBaseDatos() {
        String rutaConfigurada = System.getProperty(PROPIEDAD_RUTA_BASE_DATOS);
        if (rutaConfigurada != null && !rutaConfigurada.trim().isEmpty()) {
            return Paths.get(rutaConfigurada.trim());
        }
        String sistemaOperativo = System.getProperty("os.name", "").toLowerCase(java.util.Locale.ROOT);
        if (sistemaOperativo.contains("win")) {
            String directorioDatosAplicacion = System.getenv("APPDATA");
            if (directorioDatosAplicacion != null && !directorioDatosAplicacion.trim().isEmpty()) {
                return Paths.get(directorioDatosAplicacion, "KrelavoStore", "krelavo-store.db");
            }
        } else {
            String directorioDatosXdg = System.getenv("XDG_DATA_HOME");
            if (directorioDatosXdg != null && !directorioDatosXdg.trim().isEmpty()) {
                return Paths.get(directorioDatosXdg, "krelavo-store", "krelavo-store.db");
            }
        }
        String directorioUsuario = System.getProperty("user.home");
        if (directorioUsuario == null || directorioUsuario.trim().isEmpty()) {
            throw new IllegalStateException("No se pudo determinar la carpeta de usuario.");
        }
        return Paths.get(directorioUsuario, ".krelavo", "krelavo-store.db");
    }
}
