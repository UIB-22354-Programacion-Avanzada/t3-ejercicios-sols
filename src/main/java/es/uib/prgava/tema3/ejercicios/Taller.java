package es.uib.prgava.tema3.ejercicios;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** Ejercicio 3.2.5. Las tres operaciones del taller, cada una con su duración. Ya está escrito. */
public final class Taller {

    public static final long MILIS_ENSAMBLAR = 60;
    public static final long MILIS_PINTAR = 100;
    public static final long MILIS_EMPAQUETAR = 30;

    private static final Map<String, Set<String>> HILOS = new ConcurrentHashMap<>();

    private Taller() { }

    /** Ensambla la pieza: tarda {@link #MILIS_ENSAMBLAR} ms. */
    public static String ensamblar(String pieza) {
        trabajar("ensamblar", MILIS_ENSAMBLAR);
        return pieza + ": ensamblada";
    }

    /** Pinta la pieza: tarda {@link #MILIS_PINTAR} ms. */
    public static String pintar(String pieza) {
        trabajar("pintar", MILIS_PINTAR);
        return pieza + ", pintada";
    }

    /** Empaqueta la pieza: tarda {@link #MILIS_EMPAQUETAR} ms. */
    public static String empaquetar(String pieza) {
        trabajar("empaquetar", MILIS_EMPAQUETAR);
        return pieza + ", empaquetada";
    }

    /**
     * Para las pruebas: los nombres de los hilos que han hecho cada operación, por operación
     * ({@code "ensamblar"}, {@code "pintar"}, {@code "empaquetar"}). Es el mapa vivo: las pruebas
     * lo vacían con {@code clear()} antes de empezar.
     */
    static Map<String, Set<String>> hilosPorOperacion() {
        return HILOS;
    }

    /** Anota qué hilo hace la operación y duerme lo que esta tarda. */
    private static void trabajar(String operacion, long milis) {
        HILOS.computeIfAbsent(operacion, o -> ConcurrentHashMap.newKeySet())
                .add(Thread.currentThread().getName());
        try {
            Thread.sleep(milis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
