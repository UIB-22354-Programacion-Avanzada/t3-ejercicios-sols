package es.uib.prgava.tema3.ejercicios;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Ejercicio 3.2.2. */
class EstacionMeteoTest {

    private static final Duration PLAZO = Duration.ofMillis(500);

    private static <T> Supplier<T> tarda(long milis, T valor) {
        return () -> {
            try {
                Thread.sleep(milis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return valor;
        };
    }

    private static ExecutorService ejecutorConNombre(String prefijo) {
        AtomicInteger n = new AtomicInteger();
        return Executors.newFixedThreadPool(2, r -> new Thread(r, prefijo + n.incrementAndGet()));
    }

    @Test
    void combinaLasDosRespuestas() {
        try (var ejecutor = ejecutorConNombre("meteo-")) {
            assertEquals("temperatura 21.5, humedad 60",
                    EstacionMeteo.informe(() -> 21.5, () -> 60, PLAZO, ejecutor).join());
            assertEquals("temperatura -3.0, humedad 100",
                    EstacionMeteo.informe(() -> -3.0, () -> 100, PLAZO, ejecutor).join());
            assertEquals("temperatura 19.3, humedad 45",
                    EstacionMeteo.informe(() -> 19.25 + 0.04, () -> 45, PLAZO, ejecutor).join());
        }
    }

    @Test
    void unaConsultaQueFallaApareceComoInterrogacion() {
        try (var ejecutor = ejecutorConNombre("meteo-")) {
            Supplier<Double> rota = () -> { throw new IllegalStateException("sensor roto"); };
            assertEquals("temperatura ?, humedad 60", EstacionMeteo.informe(rota, () -> 60, PLAZO, ejecutor).join());
            Supplier<Integer> rotaH = () -> { throw new IllegalStateException("sensor roto"); };
            assertEquals("temperatura 20.0, humedad ?", EstacionMeteo.informe(() -> 20.0, rotaH, PLAZO, ejecutor).join());
        }
    }

    @Test
    void unaConsultaLentaNoRetrasaElInforme() {
        try (var ejecutor = ejecutorConNombre("meteo-")) {
            long inicio = System.nanoTime();
            String informe = EstacionMeteo.informe(tarda(3000, 18.0), tarda(100, 70), PLAZO, ejecutor).join();
            long milis = (System.nanoTime() - inicio) / 1_000_000;
            assertEquals("temperatura ?, humedad 70", informe);
            assertTrue(milis < PLAZO.toMillis() + 400, "el informe ha tardado " + milis + " ms; el plazo era " + PLAZO.toMillis());
            ejecutor.shutdownNow();
        }
    }

    @Test
    void lasDosConsultasVanALaVez() {
        try (var ejecutor = ejecutorConNombre("meteo-")) {
            long inicio = System.nanoTime();
            EstacionMeteo.informe(tarda(300, 10.0), tarda(300, 50), PLAZO, ejecutor).join();
            long milis = (System.nanoTime() - inicio) / 1_000_000;
            assertTrue(milis < 500, "dos consultas de 300 ms han tardado " + milis + " ms: no van a la vez");
        }
    }

    /** Esta es la que falla si el método espera a las consultas antes de volver. */
    @Test
    void elMetodoVuelveAlInstanteYUsaElEjecutorRecibido() {
        Set<String> hilos = ConcurrentHashMap.newKeySet();
        try (var ejecutor = ejecutorConNombre("meteo-")) {
            Supplier<Double> t = () -> { hilos.add(Thread.currentThread().getName()); return tarda(300, 1.0).get(); };
            Supplier<Integer> h = () -> { hilos.add(Thread.currentThread().getName()); return tarda(300, 2).get(); };
            long inicio = System.nanoTime();
            var futuro = EstacionMeteo.informe(t, h, PLAZO, ejecutor);
            long milis = (System.nanoTime() - inicio) / 1_000_000;
            assertTrue(milis < 100, "informe ha tardado " + milis + " ms en devolver el futuro: no debe esperar");
            assertEquals("temperatura 1.0, humedad 2", futuro.join());
            assertTrue(!hilos.isEmpty() && hilos.stream().allMatch(n -> n.startsWith("meteo-")),
                    "las consultas deben ejecutarse en el ejecutor recibido; se ejecutaron en " + hilos);
        }
    }
}
