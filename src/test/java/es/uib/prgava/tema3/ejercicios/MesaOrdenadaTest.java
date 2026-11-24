package es.uib.prgava.tema3.ejercicios;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

/** Ejercicio 3.1.3. */
class MesaOrdenadaTest {

    /** Sienta a los filósofos, los hace comer y devuelve cuántas veces dos vecinos compartieron tenedor. */
    private static long cena(Mesa mesa, int bocados, AtomicLong comidas) throws InterruptedException {
        int n = mesa.comensales();
        AtomicIntegerArray enUso = new AtomicIntegerArray(n);
        AtomicLong choques = new AtomicLong();
        Thread[] filosofos = new Thread[n];
        for (int i = 0; i < n; i++) {
            int yo = i;
            int izquierdo = yo;
            int derecho = (yo + 1) % n;
            filosofos[i] = new Thread(() -> {
                for (int k = 0; k < bocados; k++) {
                    mesa.comer(yo, () -> {
                        if (enUso.getAndIncrement(izquierdo) != 0) {
                            choques.incrementAndGet();
                        }
                        if (derecho != izquierdo && enUso.getAndIncrement(derecho) != 0) {
                            choques.incrementAndGet();
                        }
                        comidas.incrementAndGet();
                        enUso.decrementAndGet(izquierdo);
                        if (derecho != izquierdo) {
                            enUso.decrementAndGet(derecho);
                        }
                    });
                }
            });
            filosofos[i].setDaemon(true);
        }
        for (Thread f : filosofos) {
            f.start();
        }
        for (Thread f : filosofos) {
            f.join();
        }
        return choques.get();
    }

    @Test
    void laMesaTieneLosComensalesPedidos() {
        assertEquals(5, new MesaOrdenada(5).comensales());
        assertThrows(IllegalArgumentException.class, () -> new MesaOrdenada(0));
    }

    @Test
    void unFilosofoQueNoExisteEsRechazado() {
        var mesa = new MesaOrdenada(3);
        assertThrows(IllegalArgumentException.class, () -> mesa.comer(3, () -> { }));
        assertThrows(IllegalArgumentException.class, () -> mesa.comer(-1, () -> { }));
    }

    @Test
    void elBocadoSeEjecuta() {
        var mesa = new MesaOrdenada(3);
        int[] veces = new int[1];
        mesa.comer(2, () -> veces[0]++);
        assertEquals(1, veces[0]);
    }

    @Test
    void dosVecinosNuncaCompartenTenedor() throws InterruptedException {
        AtomicLong comidas = new AtomicLong();
        long choques = assertTimeoutPreemptively(Duration.ofSeconds(10), () -> cena(new MesaOrdenada(5), 20_000, comidas),
                "la cena no ha terminado en 10 s: interbloqueo");
        assertEquals(0, choques, "dos vecinos han comido a la vez");
        assertEquals(100_000, comidas.get());
    }

    /** Esta es la que falla si queda el interbloqueo: con la mesa ingenua no termina. */
    @Test
    void laCenaTerminaSiempre() {
        for (int n : new int[] {1, 2, 5, 7}) {
            AtomicLong comidas = new AtomicLong();
            assertTimeoutPreemptively(Duration.ofSeconds(10), () -> cena(new MesaOrdenada(n), 200_000, comidas),
                    "la cena de " + n + " no ha terminado en 10 s: interbloqueo");
            assertEquals(200_000L * n, comidas.get());
        }
    }
}
