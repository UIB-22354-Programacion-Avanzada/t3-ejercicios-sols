// DemoCreadorDeHilos.java
package es.uib.prgava.tema3.ejercicios;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Ejercicio 3.1.1. Crea un hilo de cada forma, los ejecuta a la vez e imprime en qué orden
 * anotaron su nombre. Lo repite cinco veces.
 *
 * <p>Mientras el ejercicio esté sin resolver, termina con la UnsupportedOperationException del
 * primer TODO que encuentre. Ya está escrito: no hace falta tocarlo.
 */
public final class DemoCreadorDeHilos {

    private DemoCreadorDeHilos() { }

    public static void main(String[] args) throws InterruptedException {
        for (int vez = 1; vez <= 5; vez++) {
            List<String> registro = new CopyOnWriteArrayList<>();
            List<Thread> hilos = List.of(
                    CreadorDeHilos.conSubclase("forma-1", registro),
                    CreadorDeHilos.conClaseRunnable("forma-2", registro),
                    CreadorDeHilos.conLambda("forma-3", registro),
                    CreadorDeHilos.conConstructor("forma-4", registro));
            if (vez == 1) {
                System.out.println("   antes de ejecutarlos, el primero esta " + hilos.get(0).getState());
            }
            CreadorDeHilos.ejecutarTodos(hilos);
            System.out.println("   vuelta " + vez + ": " + registro);
        }
    }
}
