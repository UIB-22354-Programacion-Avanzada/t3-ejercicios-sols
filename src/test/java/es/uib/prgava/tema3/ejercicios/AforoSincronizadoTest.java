package es.uib.prgava.tema3.ejercicios;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Ejercicio 3.1.2. */
class AforoSincronizadoTest {

    /** Un aforo de mentira que no deja entrar a nadie y anota quién lo llama. */
    private static final class Espia implements ControlDeAforo {
        final AtomicInteger llamadas = new AtomicInteger();
        final Set<Thread> hilos = ConcurrentHashMap.newKeySet();

        @Override
        public boolean entrar() {
            llamadas.incrementAndGet();
            hilos.add(Thread.currentThread());
            return false;
        }

        @Override
        public int dentro() {
            return 0;
        }

        @Override
        public int capacidad() {
            return 0;
        }
    }

    @Test
    void conUnSoloHiloSeComportaComoElOriginal() {
        var aforo = new AforoSincronizado(2);
        assertTrue(aforo.entrar());
        assertTrue(aforo.entrar());
        assertFalse(aforo.entrar());
        assertEquals(2, aforo.dentro());
        assertEquals(2, aforo.capacidad());
        assertThrows(IllegalArgumentException.class, () -> new AforoSincronizado(-1));
    }

    @Test
    void laSimulacionLanzaLosHilosYHaceTodosLosIntentos() throws InterruptedException {
        var espia = new Espia();
        assertEquals(0, SimulacionAforo.entradasAceptadas(espia, 4, 1000));
        assertEquals(4000, espia.llamadas.get(), "cada hilo debe intentar entrar intentosPorHilo veces");
        assertEquals(4, espia.hilos.size(), "debe haber un hilo por cada uno de los pedidos");
        assertFalse(espia.hilos.contains(Thread.currentThread()), "los intentos no se hacen desde el hilo que llama");
    }

    @Test
    void laSimulacionCuentaBienLasAceptadas() throws InterruptedException {
        assertEquals(10, SimulacionAforo.entradasAceptadas(new AforoSincronizado(10), 1, 50));
        assertEquals(0, SimulacionAforo.entradasAceptadas(new AforoSincronizado(0), 3, 50));
        assertThrows(IllegalArgumentException.class, () -> SimulacionAforo.entradasAceptadas(new AforoSincronizado(1), 0, 5));
    }

    /** Con el Aforo original, la simulación debe pillar la carrera alguna vez en veinte intentos. */
    @Test
    void laSimulacionPillaLaCarreraDelAforoOriginal() throws InterruptedException {
        int pilladas = 0;
        for (int vez = 0; vez < 20; vez++) {
            var aforo = new Aforo(1000);
            int aceptadas = SimulacionAforo.entradasAceptadas(aforo, 8, 500);
            if (aceptadas > aforo.capacidad()) {
                pilladas++;
            }
        }
        assertTrue(pilladas > 0, "en veinte simulaciones, Aforo nunca dejo entrar a mas de la cuenta");
    }

    /** Esta es la que falla si queda alguna carrera. */
    @Test
    void conVariosHilosNoEntraNadieDeMas() throws InterruptedException {
        for (int vez = 0; vez < 20; vez++) {
            var aforo = new AforoSincronizado(1000);
            int aceptadas = SimulacionAforo.entradasAceptadas(aforo, 8, 500);
            assertEquals(1000, aceptadas, "en la simulacion " + (vez + 1) + " entraron de mas o de menos");
            assertEquals(1000, aforo.dentro());
        }
    }
}
