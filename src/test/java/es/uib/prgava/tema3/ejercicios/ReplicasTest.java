package es.uib.prgava.tema3.ejercicios;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Ejercicio 3.3.2. Usa StructuredTaskScope: hay que compilar y ejecutar con --enable-preview. */
class ReplicasTest {

    private static final Duration PLAZO = Duration.ofSeconds(2);

    /** Réplicas simuladas que anotan cuántas siguen en marcha y cuántas han sido canceladas. */
    private static final class Granja {
        final AtomicInteger enMarcha = new AtomicInteger();
        final AtomicInteger canceladas = new AtomicInteger();

        Callable<String> contesta(long milis, String respuesta) {
            return () -> {
                enMarcha.incrementAndGet();
                try {
                    Thread.sleep(milis);
                    return respuesta;
                } catch (InterruptedException e) {
                    canceladas.incrementAndGet();
                    throw e;
                } finally {
                    enMarcha.decrementAndGet();
                }
            };
        }

        Callable<String> falla(long milis) {
            return () -> {
                enMarcha.incrementAndGet();
                try {
                    Thread.sleep(milis);
                    throw new IllegalStateException("replica caida");
                } finally {
                    enMarcha.decrementAndGet();
                }
            };
        }
    }

    private static String preguntar(List<Callable<String>> replicas, Duration plazo) {
        return assertTimeoutPreemptively(Duration.ofSeconds(10), () -> Replicas.primeraRespuesta(replicas, plazo));
    }

    @Test
    void devuelveLaRespuestaMasRapida() {
        var granja = new Granja();
        assertEquals("b", preguntar(List.of(granja.contesta(300, "a"), granja.contesta(50, "b"), granja.contesta(200, "c")), PLAZO));
    }

    @Test
    void ignoraLasReplicasQueFallan() {
        var granja = new Granja();
        assertEquals("lenta", preguntar(List.of(granja.falla(10), granja.falla(20), granja.contesta(150, "lenta")), PLAZO));
    }

    /** Esta es la que falla si queda alguna consulta en marcha al volver. */
    @Test
    void alVolverNoQuedaNingunaConsultaEnMarcha() {
        var granja = new Granja();
        long inicio = System.nanoTime();
        preguntar(List.of(granja.contesta(50, "rapida"), granja.contesta(3000, "x"), granja.contesta(3000, "y")), PLAZO);
        long milis = (System.nanoTime() - inicio) / 1_000_000;
        assertEquals(0, granja.enMarcha.get(), "al volver, todavia habia consultas en marcha");
        assertEquals(2, granja.canceladas.get(), "las dos replicas lentas debian ser canceladas");
        assertTrue(milis < 1000, "con una replica de 50 ms ha tardado " + milis + " ms: no se cancelan las demas");
    }

    @Test
    void siFallanTodasLanzaFailedException() {
        var granja = new Granja();
        assertThrows(StructuredTaskScope.FailedException.class,
                () -> Replicas.primeraRespuesta(List.of(granja.falla(10), granja.falla(30)), PLAZO));
    }

    @Test
    void siNingunaContestaATiempoLanzaTimeoutException() {
        var granja = new Granja();
        long inicio = System.nanoTime();
        assertThrows(StructuredTaskScope.TimeoutException.class,
                () -> Replicas.primeraRespuesta(List.of(granja.contesta(3000, "a"), granja.contesta(3000, "b")), Duration.ofMillis(200)));
        long milis = (System.nanoTime() - inicio) / 1_000_000;
        assertTrue(milis < 1000, "con un plazo de 200 ms ha tardado " + milis + " ms");
        assertEquals(0, granja.enMarcha.get(), "al volver, todavia habia consultas en marcha");
    }

    @Test
    void sinReplicasEsRechazado() {
        assertThrows(IllegalArgumentException.class, () -> Replicas.primeraRespuesta(List.<Callable<String>>of(), PLAZO));
    }
}
