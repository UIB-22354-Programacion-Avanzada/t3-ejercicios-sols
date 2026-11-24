package es.uib.prgava.tema3.ejercicios;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Ejercicio 3.3.1. */
class DescargadorTest {

    /** Una descarga simulada que tarda {@code milis} y anota cuántas hay en curso a la vez. */
    private static final class Simulada implements Function<String, String> {
        final long milis;
        final AtomicInteger enCurso = new AtomicInteger();
        final AtomicInteger maximo = new AtomicInteger();
        final AtomicBoolean algunaEnPlataforma = new AtomicBoolean();

        Simulada(long milis) {
            this.milis = milis;
        }

        @Override
        public String apply(String direccion) {
            if (!Thread.currentThread().isVirtual()) {
                algunaEnPlataforma.set(true);
            }
            maximo.accumulateAndGet(enCurso.incrementAndGet(), Math::max);
            try {
                Thread.sleep(milis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                enCurso.decrementAndGet();
            }
            return "contenido de " + direccion;
        }
    }

    private static List<String> direcciones(int n) {
        List<String> todas = new ArrayList<>();
        for (int i = 1; i <= n; i++) {
            todas.add("https://ejemplo.org/" + i);
        }
        return todas;
    }

    private static Map<String, String> descargar(List<String> direcciones, int simultaneas, Function<String, String> f) {
        return assertTimeoutPreemptively(Duration.ofSeconds(10), () -> Descargador.descargarTodas(direcciones, simultaneas, f));
    }

    @Test
    void descargaTodasLasDirecciones() {
        Map<String, String> contenidos = descargar(direcciones(30), 5, new Simulada(10));
        assertEquals(30, contenidos.size());
        for (String direccion : direcciones(30)) {
            assertEquals("contenido de " + direccion, contenidos.get(direccion));
        }
    }

    @Test
    void cadaDescargaVaEnUnHiloVirtual() {
        var simulada = new Simulada(5);
        descargar(direcciones(20), 4, simulada);
        assertFalse(simulada.algunaEnPlataforma.get(), "alguna descarga se ha hecho en un hilo de plataforma");
    }

    /** Esta es la que falla si el semáforo no limita, o si limita de más. */
    @Test
    void nuncaHayMasDescargasALaVezQueLasPermitidas() {
        for (int simultaneas : new int[] {1, 3, 10}) {
            var simulada = new Simulada(20);
            descargar(direcciones(40), simultaneas, simulada);
            assertEquals(simultaneas, simulada.maximo.get(), "con un limite de " + simultaneas + " descargas a la vez");
        }
    }

    @Test
    void milDescargasALaVezTardanComoUna() {
        var simulada = new Simulada(200);
        long inicio = System.nanoTime();
        descargar(direcciones(1000), 1000, simulada);
        long milis = (System.nanoTime() - inicio) / 1_000_000;
        assertTrue(milis < 1500, "mil descargas de 200 ms sin limite efectivo han tardado " + milis + " ms");
    }

    @Test
    void unaDescargaQueFallaNoTumbaALasDemas() {
        Function<String, String> aveces = d -> {
            if (d.endsWith("/3")) {
                throw new IllegalStateException("servidor caido");
            }
            return "ok";
        };
        Map<String, String> contenidos = descargar(direcciones(5), 2, aveces);
        assertEquals("error: servidor caido", contenidos.get("https://ejemplo.org/3"));
        assertEquals("ok", contenidos.get("https://ejemplo.org/5"));
        assertEquals(5, contenidos.size());
    }

    @Test
    void unLimiteSinSentidoEsRechazado() {
        assertThrows(IllegalArgumentException.class, () -> Descargador.descargarTodas(direcciones(1), 0, d -> d));
    }
}
