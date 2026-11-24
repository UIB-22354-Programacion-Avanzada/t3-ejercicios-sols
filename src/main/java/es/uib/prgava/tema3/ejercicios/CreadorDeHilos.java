package es.uib.prgava.tema3.ejercicios;

import java.util.List;
import java.util.Objects;

/**
 * Ejercicio 3.1.1. Las cuatro formas de crear un hilo, y cómo arrancar y esperar a varios.
 *
 * <p>Los cuatro métodos {@code con…} crean un hilo <strong>sin arrancarlo</strong>, con el nombre
 * que se les da, cuya tarea es anotar su nombre en el registro. El modelo es el ejemplo «Cuatro
 * formas de crear un hilo» del tema.
 */
public final class CreadorDeHilos {

    private CreadorDeHilos() { }

    /** Forma 1: una subclase de {@code Thread} que redefine {@code run}. */
    static final class HiloAnotador extends Thread {

        private final List<String> registro;

        HiloAnotador(String nombre, List<String> registro) {
            super(nombre);
            this.registro = Objects.requireNonNull(registro);
        }

        @Override
        public void run() {
            registro.add(getName());
        }
    }

    /** Forma 2: una clase con nombre que implementa {@code Runnable}. */
    static final class TareaAnotar implements Runnable {

        private final List<String> registro;

        TareaAnotar(List<String> registro) {
            this.registro = Objects.requireNonNull(registro);
        }

        @Override
        public void run() {
            registro.add(Thread.currentThread().getName());
        }
    }

    /** Forma 1: con la subclase {@link HiloAnotador}. */
    public static Thread conSubclase(String nombre, List<String> registro) {
        return new HiloAnotador(nombre, registro);
    }

    /** Forma 2: con un {@code Thread} que recibe un {@link TareaAnotar}. */
    public static Thread conClaseRunnable(String nombre, List<String> registro) {
        return new Thread(new TareaAnotar(registro), nombre);
    }

    /** Forma 3: con un {@code Thread} que recibe una lambda. */
    public static Thread conLambda(String nombre, List<String> registro) {
        Objects.requireNonNull(registro);
        return new Thread(() -> registro.add(Thread.currentThread().getName()), nombre);
    }

    /** Forma 4: con el constructor de hilos, {@code Thread.ofPlatform()}. */
    public static Thread conConstructor(String nombre, List<String> registro) {
        Objects.requireNonNull(registro);
        return Thread.ofPlatform().name(nombre).unstarted(
                () -> registro.add(Thread.currentThread().getName()));
    }

    /** Arranca todos los hilos, de modo que se ejecuten a la vez, y vuelve cuando han terminado todos. */
    public static void ejecutarTodos(List<Thread> hilos) throws InterruptedException {
        for (Thread hilo : hilos) {
            hilo.start();
        }
        for (Thread hilo : hilos) {
            hilo.join();
        }
    }

    // ----------------------------------------------------------------------------------
    // LO QUE DISTINGUE A LAS CUATRO FORMAS. En la forma 1 el objeto ES el hilo, y por eso run()
    // puede usar getName(). En las otras tres el objeto es una TAREA, que no sabe en que hilo va a
    // ejecutarse, y tiene que preguntarlo con Thread.currentThread(). La forma 4 usa unstarted y no
    // start: los cuatro metodos devuelven el hilo sin arrancar, y quien lo arranca es ejecutarTodos.
    //
    // POR QUE DOS BUCLES EN ejecutarTodos. join bloquea hasta que ese hilo termina. Con start y
    // join en la misma vuelta, cada hilo terminaria antes de que arrancara el siguiente: los cuatro
    // irian uno detras de otro y el programa seria secuencial con mas ceremonia.
    //
    // RESPUESTA al «Para pensar»: ¿que habria que cambiar para ejecutar la tarea en un ejecutor?
    //
    // En las formas 2, 3 y 4, nada de la tarea: TareaAnotar o la lambda son Runnable, y un
    // ejecutor recibe Runnable. Se entrega con ejecutor.execute(tarea) en lugar de envolverla en un
    // Thread. En la forma 1 hay que reescribir la clase: la tarea esta dentro de una subclase de
    // Thread, mezclada con el hilo, y hay que sacarla a un Runnable. (Tecnicamente un Thread es un
    // Runnable y execute(hilo) compila, pero el ejecutor solo llama a su run() en uno de sus hilos;
    // el objeto Thread no llega a arrancar nunca, y crearlo no ha servido para nada.) Es la razon
    // de que la forma 1 casi no se use: separar el que del quien es lo que permite cambiar el quien.
    // ----------------------------------------------------------------------------------
}
