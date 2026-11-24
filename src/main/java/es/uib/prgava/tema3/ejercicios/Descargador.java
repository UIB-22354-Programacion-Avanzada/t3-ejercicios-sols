package es.uib.prgava.tema3.ejercicios;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.function.Function;

/** Ejercicio 3.3.1. Muchas descargas, cada una en su hilo virtual, con un límite de simultáneas. */
public final class Descargador {

    private Descargador() { }

    /**
     * Descarga todas las direcciones con {@code descargar}, cada una en su propio hilo virtual, y
     * nunca más de {@code simultaneas} a la vez. Devuelve el contenido de cada dirección; si una
     * descarga lanza una excepción, su contenido es {@code "error: "} seguido del mensaje.
     */
    public static Map<String, String> descargarTodas(List<String> direcciones, int simultaneas,
            Function<String, String> descargar) throws InterruptedException {
        if (simultaneas < 1) {
            throw new IllegalArgumentException("Hace falta al menos una descarga simultanea");
        }
        Map<String, String> contenidos = new ConcurrentHashMap<>();
        // Un hilo virtual por descarga, sin limite: crear diez mil cuesta poco. El limite lo pone el
        // semaforo, que es donde esta el recurso escaso: las descargas a la vez, no los hilos.
        Semaphore permisos = new Semaphore(simultaneas);
        try (ExecutorService ejecutor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (String direccion : direcciones) {
                ejecutor.execute(() -> {
                    try {
                        permisos.acquire();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                    try {
                        contenidos.put(direccion, descargar.apply(direccion));
                    } catch (RuntimeException e) {
                        contenidos.put(direccion, "error: " + e.getMessage());
                    } finally {
                        // En un finally: un permiso que no se devuelve se pierde para siempre.
                        permisos.release();
                    }
                });
            }
        }
        return contenidos;
    }

    // ----------------------------------------------------------------------------------
    // RESPUESTA al «Para pensar»: dos descargadores contra el mismo servidor.
    //
    // El servidor ve hasta veinte conexiones, diez de cada uno: el semaforo limita a cada
    // descargador, no al servidor, porque cada uno tiene el suyo. Para que el limite sea del
    // servidor, el semaforo tiene que ser UNO y compartido por todos los que le hablan: se crea
    // fuera, uno por servidor —por ejemplo, en un mapa de host a semaforo—, y se le pasa a cada
    // descargador en lugar de crearlo dentro. Es la regla de 3.3.3: el limite va donde esta el
    // recurso escaso, y aqui el recurso escaso es el servidor, no el descargador.
    // ----------------------------------------------------------------------------------
}
