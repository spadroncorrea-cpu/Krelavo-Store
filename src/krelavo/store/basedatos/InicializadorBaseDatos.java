package krelavo.store.basedatos;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import krelavo.store.basedatos.migraciones.MigracionCatalogoInicial;
import krelavo.store.basedatos.migraciones.MigracionEsquemaInicial;
import krelavo.store.basedatos.migraciones.MigracionExperienciaCliente;
import krelavo.store.basedatos.migraciones.MigracionInventarioInicial;
import krelavo.store.basedatos.migraciones.MigracionProteccionRoles;

/** Aplica las migraciones SQLite pendientes según PRAGMA user_version. */
public final class InicializadorBaseDatos {
    private final FabricaConexiones fabricaConexiones;
    private final List<MigracionEsquema> migraciones;

    public InicializadorBaseDatos() {
        this(new FabricaConexiones());
    }

    public InicializadorBaseDatos(FabricaConexiones fabricaConexiones) {
        if (fabricaConexiones == null) {
            throw new IllegalArgumentException("La fábrica de conexiones es obligatoria.");
        }
        this.fabricaConexiones = fabricaConexiones;
        this.migraciones = Collections.unmodifiableList(Arrays.<MigracionEsquema>asList(
                new MigracionEsquemaInicial(), new MigracionCatalogoInicial(),
                new MigracionInventarioInicial(), new MigracionProteccionRoles(),
                new MigracionExperienciaCliente()));
    }

    public void inicializar() throws SQLException {
        try (Connection conexion = fabricaConexiones.abrirConexion()) {
            int versionActual = leerVersionEsquema(conexion);
            int ultimaVersion = migraciones.get(migraciones.size() - 1).obtenerVersion();
            if (versionActual > ultimaVersion) {
                throw new SQLException("La base de datos usa una versión más reciente (" + versionActual
                        + ") que la aplicación (" + ultimaVersion + ").");
            }

            for (MigracionEsquema migracion : migraciones) {
                if (migracion.obtenerVersion() <= versionActual) {
                    continue;
                }
                aplicarMigracion(conexion, migracion);
            }
        }
    }

    private void aplicarMigracion(Connection conexion, MigracionEsquema migracion) throws SQLException {
        conexion.setAutoCommit(false);
        try {
            migracion.aplicar(conexion);
            try (Statement sentencia = conexion.createStatement()) {
                sentencia.execute("PRAGMA user_version = " + migracion.obtenerVersion());
            }
            conexion.commit();
        } catch (SQLException ex) {
            try {
                conexion.rollback();
            } catch (SQLException errorReversion) {
                ex.addSuppressed(errorReversion);
            }
            throw ex;
        } finally {
            conexion.setAutoCommit(true);
        }
    }

    private int leerVersionEsquema(Connection conexion) throws SQLException {
        try (Statement sentencia = conexion.createStatement();
                java.sql.ResultSet resultado = sentencia.executeQuery("PRAGMA user_version")) {
            if (!resultado.next()) {
                throw new SQLException("SQLite no devolvió PRAGMA user_version.");
            }
            return resultado.getInt(1);
        }
    }
}
