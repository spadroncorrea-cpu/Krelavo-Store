package krelavo.store.basedatos;

import java.sql.Connection;
import java.sql.SQLException;

/** Contrato de una migración transaccional del esquema o los datos iniciales. */
public interface MigracionEsquema {
    int obtenerVersion();
    void aplicar(Connection conexion) throws SQLException;
}
