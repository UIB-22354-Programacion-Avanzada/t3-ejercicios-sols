package es.uib.prgava.tema3.ejercicios;

import java.util.Map;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Ejercicio 3.2.3. */
class VotacionTest {

    private static final String[] OPCIONES = {"norte", "sur", "este", "oeste"};

    /** Lanza los hilos, los suelta a la vez con una barrera y espera a que terminen. */
    private static void enParalelo(int hilos, Runnable tarea) throws InterruptedException {
        CyclicBarrier salida = new CyclicBarrier(hilos);
        Thread[] lanzados = new Thread[hilos];
        for (int i = 0; i < hilos; i++) {
            lanzados[i] = new Thread(() -> {
                try {
                    salida.await();
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
                tarea.run();
            });
            lanzados[i].start();
        }
        for (Thread hilo : lanzados) {
            hilo.join();
        }
    }

    @Test
    void conUnSoloHiloCuentaBien() {
        var votacion = new Votacion();
        votacion.votar("norte");
        votacion.votar("norte");
        votacion.votar("sur");
        assertEquals(2, votacion.votos("norte"));
        assertEquals(1, votacion.votos("sur"));
        assertEquals(0, votacion.votos("este"), "una opcion sin votos tiene 0");
        assertEquals(3, votacion.total());
        assertEquals(Map.of("norte", 2L, "sur", 1L), votacion.resultados());
    }

    @Test
    void losResultadosSonUnaCopia() {
        var votacion = new Votacion();
        votacion.votar("norte");
        Map<String, Long> foto = votacion.resultados();
        votacion.votar("norte");
        assertEquals(1, (long) foto.get("norte"), "la copia no debe cambiar al seguir votando");
        assertThrows(UnsupportedOperationException.class, () -> foto.put("sur", 5L));
    }

    /** Esta es la que falla si el recuento tiene una carrera. */
    @Test
    void conVariosHilosNoSePierdeNingunVoto() throws InterruptedException {
        for (int vez = 0; vez < 10; vez++) {
            var votacion = new Votacion();
            AtomicInteger siguiente = new AtomicInteger();
            enParalelo(8, () -> {
                for (int k = 0; k < 10_000; k++) {
                    votacion.votar(OPCIONES[Math.floorMod(siguiente.getAndIncrement(), 4)]);
                }
            });
            for (String opcion : OPCIONES) {
                assertEquals(20_000, votacion.votos(opcion), "votos de " + opcion + " en la vuelta " + (vez + 1));
            }
            assertEquals(80_000, votacion.total());
        }
    }

    @Test
    void cadaVotanteVotaUnaSolaVez() {
        var votacion = new Votacion();
        assertTrue(votacion.votarUnaVez("ana", "norte"));
        assertFalse(votacion.votarUnaVez("ana", "sur"), "ana ya habia votado");
        assertEquals(1, votacion.votos("norte"));
        assertEquals(0, votacion.votos("sur"));
        assertEquals(1, votacion.total());
    }

    /** Esta es la que falla si se comprueba y despues se actua. */
    @Test
    void cadaVotanteVotaUnaSolaVezAunqueLoIntenteDesdeVariosHilos() throws InterruptedException {
        for (int vez = 0; vez < 10; vez++) {
            var votacion = new Votacion();
            AtomicInteger aceptados = new AtomicInteger();
            enParalelo(8, () -> {
                for (int v = 0; v < 20_000; v++) {
                    if (votacion.votarUnaVez("votante-" + v, OPCIONES[v % 4])) {
                        aceptados.incrementAndGet();
                    }
                }
            });
            assertEquals(20_000, aceptados.get(), "votos aceptados en la vuelta " + (vez + 1));
            assertEquals(20_000, votacion.total());
        }
    }
}
