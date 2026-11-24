package es.uib.prgava.tema3.ejercicios;

import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Ejercicio 3.2.1. */
class PrimosEnParaleloTest {

    /** Un ejecutor que pasa las tareas a otro y cuenta cuántas le llegan. */
    private static final class Contador extends AbstractExecutorService {
        final ExecutorService real = Executors.newFixedThreadPool(4);
        final AtomicInteger tareas = new AtomicInteger();
        volatile boolean cerrado;

        @Override public void execute(Runnable tarea) { tareas.incrementAndGet(); real.execute(tarea); }
        @Override public void shutdown() { cerrado = true; real.shutdown(); }
        @Override public List<Runnable> shutdownNow() { cerrado = true; return real.shutdownNow(); }
        @Override public boolean isShutdown() { return cerrado; }
        @Override public boolean isTerminated() { return real.isTerminated(); }
        @Override public boolean awaitTermination(long t, TimeUnit u) throws InterruptedException { return real.awaitTermination(t, u); }
    }

    @Test
    void cuentaLosPrimosComoLaVersionSecuencial() throws Exception {
        try (ExecutorService ejecutor = Executors.newFixedThreadPool(4)) {
            assertEquals(9592, PrimosEnParalelo.contar(0, 100_000, 4, ejecutor));
            assertEquals(PrimosEnParalelo.contarSecuencial(1000, 54_321), PrimosEnParalelo.contar(1000, 54_321, 7, ejecutor));
            assertEquals(1, PrimosEnParalelo.contar(2, 3, 1, ejecutor));
        }
    }

    @Test
    void elRepartoNoPierdeNiRepiteNumeros() throws Exception {
        try (ExecutorService ejecutor = Executors.newFixedThreadPool(3)) {
            for (int partes = 1; partes <= 12; partes++) {
                assertEquals(25, PrimosEnParalelo.contar(0, 100, partes, ejecutor), "con " + partes + " partes");
            }
            assertEquals(2, PrimosEnParalelo.contar(0, 5, 10, ejecutor), "con mas partes que numeros");
            assertEquals(0, PrimosEnParalelo.contar(7, 7, 3, ejecutor), "con un intervalo vacio");
        }
    }

    @Test
    void entregaUnaTareaPorParteAlEjecutorYNoLoCierra() throws Exception {
        var contador = new Contador();
        PrimosEnParalelo.contar(0, 10_000, 6, contador);
        assertEquals(6, contador.tareas.get(), "debe entregar exactamente una tarea por parte al ejecutor recibido");
        assertFalse(contador.isShutdown(), "el ejecutor no es del metodo: no debe cerrarlo");
        contador.shutdown();
    }

    @Test
    void losArgumentosSinSentidoSonRechazados() {
        try (ExecutorService ejecutor = Executors.newFixedThreadPool(1)) {
            assertThrows(IllegalArgumentException.class, () -> PrimosEnParalelo.contar(10, 5, 2, ejecutor));
            assertThrows(IllegalArgumentException.class, () -> PrimosEnParalelo.contar(0, 5, 0, ejecutor));
        }
    }
}
