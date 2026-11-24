package es.uib.prgava.tema3.ejercicios;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Ejercicio 3.1.1. */
class CreadorDeHilosTest {

    private static final List<BiFunction<String, List<String>, Thread>> FORMAS = List.of(
            CreadorDeHilos::conSubclase, CreadorDeHilos::conClaseRunnable,
            CreadorDeHilos::conLambda, CreadorDeHilos::conConstructor);

    @Test
    void lasCuatroFormasCreanElHiloSinArrancarlo() {
        for (var forma : FORMAS) {
            Thread hilo = forma.apply("prueba", new CopyOnWriteArrayList<>());
            assertEquals(Thread.State.NEW, hilo.getState(), "el hilo no debe estar arrancado");
            assertEquals("prueba", hilo.getName(), "el hilo debe llevar el nombre que se le da");
        }
    }

    @Test
    void cadaHiloAnotaSuNombreDesdeSuPropioHilo() throws InterruptedException {
        for (var forma : FORMAS) {
            List<String> registro = new CopyOnWriteArrayList<>();
            Thread hilo = forma.apply("anotador", registro);
            hilo.start();
            hilo.join();
            assertEquals(List.of("anotador"), registro,
                    "el hilo debe anotar en el registro el nombre del hilo que lo ejecuta");
        }
    }

    @Test
    void laFormaUnoEsUnaSubclaseDeThread() {
        Thread hilo = CreadorDeHilos.conSubclase("x", new CopyOnWriteArrayList<>());
        assertNotEquals(Thread.class, hilo.getClass(), "conSubclase debe devolver una subclase propia de Thread");
        assertEquals(CreadorDeHilos.HiloAnotador.class, hilo.getClass());
    }

    @Test
    void laFormaCuatroUsaElConstructorDeHilos() {
        Thread hilo = CreadorDeHilos.conConstructor("x", new CopyOnWriteArrayList<>());
        assertEquals(Thread.class, hilo.getClass(), "Thread.ofPlatform() crea un Thread corriente");
        assertTrue(!hilo.isVirtual(), "debe ser un hilo de plataforma");
    }

    @Test
    void ejecutarTodosLosEsperaATodos() throws InterruptedException {
        List<String> registro = new CopyOnWriteArrayList<>();
        List<Thread> hilos = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            hilos.add(FORMAS.get(i).apply("h" + i, registro));
        }
        CreadorDeHilos.ejecutarTodos(hilos);
        assertEquals(4, registro.size(), "al volver, los cuatro hilos deben haber terminado");
        for (Thread hilo : hilos) {
            assertEquals(Thread.State.TERMINATED, hilo.getState());
        }
    }

    /** Esta es la que falla si se llama a run en lugar de start, o si se espera dentro del primer bucle. */
    @Test
    void ejecutarTodosLosEjecutaALaVez() {
        int n = 4;
        CyclicBarrier todosALaVez = new CyclicBarrier(n);
        List<String> problemas = new CopyOnWriteArrayList<>();
        AtomicReference<Thread> llamante = new AtomicReference<>();
        List<Thread> hilos = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            hilos.add(new Thread(() -> {
                if (Thread.currentThread() == llamante.get()) {
                    problemas.add("una tarea se ha ejecutado en el hilo que llama: run en lugar de start");
                }
                try {
                    todosALaVez.await(2, TimeUnit.SECONDS);
                } catch (Exception e) {
                    problemas.add("los hilos no estaban en marcha a la vez");
                }
            }));
        }
        assertTimeoutPreemptively(Duration.ofSeconds(10), () -> {
            llamante.set(Thread.currentThread());
            CreadorDeHilos.ejecutarTodos(hilos);
        });
        assertEquals(List.of(), problemas.stream().distinct().toList());
    }
}
