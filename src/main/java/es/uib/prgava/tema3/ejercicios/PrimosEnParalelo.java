package es.uib.prgava.tema3.ejercicios;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/** Ejercicio 3.2.1. Contar primos repartiendo el intervalo entre las tareas de un ejecutor. */
public final class PrimosEnParalelo {

    private PrimosEnParalelo() { }

    /** Si {@code n} es primo. Ya está escrito: no hace falta tocarlo. */
    public static boolean esPrimo(int n) {
        if (n < 2) {
            return false;
        }
        for (int d = 2; (long) d * d <= n; d++) {
            if (n % d == 0) {
                return false;
            }
        }
        return true;
    }

    /** Cuántos primos hay en {@code [desde, hasta)}, contados secuencialmente. Ya está escrito. */
    public static long contarSecuencial(int desde, int hasta) {
        long cuenta = 0;
        for (int n = desde; n < hasta; n++) {
            if (esPrimo(n)) {
                cuenta++;
            }
        }
        return cuenta;
    }

    /**
     * Cuántos primos hay en {@code [desde, hasta)}, repartiendo el intervalo en {@code partes}
     * tareas que se entregan a {@code ejecutor}. No cierra el ejecutor: no es suyo.
     */
    public static long contar(int desde, int hasta, int partes, ExecutorService ejecutor)
            throws InterruptedException, ExecutionException {
        if (desde > hasta || partes < 1) {
            throw new IllegalArgumentException("Intervalo vacio al reves o numero de partes no positivo");
        }
        // Una tarea por parte. Los limites se calculan con long para que longitud * k no desborde
        // con intervalos grandes, y la formula reparte el resto entre las partes: trozos
        // contiguos, sin solapes, que cubren el intervalo aunque no sea multiplo de partes.
        List<Callable<Long>> tareas = new ArrayList<>();
        int longitud = hasta - desde;
        for (int k = 0; k < partes; k++) {
            int inicio = desde + (int) ((long) longitud * k / partes);
            int fin = desde + (int) ((long) longitud * (k + 1) / partes);
            tareas.add(() -> contarSecuencial(inicio, fin));
        }
        // invokeAll entrega todas las tareas y espera a que terminen; los futuros que devuelve ya
        // tienen su resultado, asi que get() no bloquea. El ejecutor no se cierra: no es de este
        // metodo, lo cierra quien lo creo.
        long total = 0;
        for (Future<Long> futuro : ejecutor.invokeAll(tareas)) {
            total += futuro.get();
        }
        return total;
    }

    // ----------------------------------------------------------------------------------
    // RESPUESTA al «Para pensar»: ¿por que 16 partes pueden ir mas rapido que 4 con 4 hilos?
    //
    // Porque no todos los numeros cuestan lo mismo: comprobar si n es primo cuesta hasta raiz de n
    // divisiones, y los numeros grandes cuestan mas. Con cuatro partes iguales, la ultima es la mas
    // cara y los otros tres hilos terminan antes y se quedan parados esperandola. Con dieciseis
    // partes, cada hilo que termina coge la siguiente, y el trabajo se reparte solo: es equilibrio
    // de carga. Y un millon de partes seria peor, porque cada tarea tiene un coste fijo —crearla,
    // encolarla, su futuro, sacarla de la cola— que con trozos de diez numeros ya es mayor que el
    // trabajo util. Hay un punto medio, y suele estar en unas pocas veces el numero de hilos.
    // ----------------------------------------------------------------------------------
}
