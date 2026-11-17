package krelavo.store.servicio;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.concurrent.Task;

/** Ejecuta autenticación y hash de contraseñas sin bloquear JavaFX. */
public final class EjecutorAcceso {
    private static final AtomicInteger CONTADOR_HILOS = new AtomicInteger();
    private static final ExecutorService EJECUTOR = Executors.newFixedThreadPool(2,
            new ThreadFactory() {
                public Thread newThread(Runnable trabajo) {
                    Thread hilo = new Thread(trabajo,
                            "acceso-krelavo-" + CONTADOR_HILOS.incrementAndGet());
                    hilo.setDaemon(true);
                    return hilo;
                }
            });

    private EjecutorAcceso() { }

    public static <T> Task<T> crearTarea(final Callable<T> operacion) {
        return new Task<T>() {
            @Override
            protected T call() throws Exception {
                return operacion.call();
            }
        };
    }

    public static void ejecutar(Task<?> tarea) {
        EJECUTOR.execute(tarea);
    }
}
