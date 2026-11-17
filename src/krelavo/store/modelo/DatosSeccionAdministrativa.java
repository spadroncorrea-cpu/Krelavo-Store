package krelavo.store.modelo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Datos tabulares de una sección del panel administrativo. */
public final class DatosSeccionAdministrativa {
    private final List<String> encabezados;
    private final List<List<String>> filas;

    public DatosSeccionAdministrativa(List<String> encabezados, List<List<String>> filas) {
        this.encabezados = Collections.unmodifiableList(new ArrayList<String>(encabezados));
        List<List<String>> copia = new ArrayList<List<String>>();
        for (List<String> fila : filas) copia.add(Collections.unmodifiableList(new ArrayList<String>(fila)));
        this.filas = Collections.unmodifiableList(copia);
    }
    public List<String> obtenerEncabezados() { return encabezados; }
    public List<List<String>> obtenerFilas() { return filas; }
}
