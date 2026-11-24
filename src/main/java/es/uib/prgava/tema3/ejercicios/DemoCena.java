package es.uib.prgava.tema3.ejercicios;

/**
 * Ejercicio 3.1.3. Cinco filósofos comen cien mil veces cada uno en una mesa.
 *
 * <p>Con {@link MesaIngenua}, lo normal es que el programa se quede parado a los pocos segundos:
 * es el interbloqueo del ejercicio. Cambia la mesa por {@code new MesaOrdenada(5)} cuando la tengas.
 */
public final class DemoCena {

    private static final int BOCADOS = 100_000;

    private DemoCena() { }

    public static void main(String[] args) throws InterruptedException {
        Mesa mesa = new MesaIngenua(5);
        long[] comidos = new long[mesa.comensales()];
        Thread[] filosofos = new Thread[mesa.comensales()];
        for (int i = 0; i < filosofos.length; i++) {
            int yo = i;
            filosofos[i] = new Thread(() -> {
                for (int k = 0; k < BOCADOS; k++) {
                    mesa.comer(yo, () -> comidos[yo]++);
                }
            }, "filosofo-" + i);
        }
        for (Thread filosofo : filosofos) {
            filosofo.start();
        }
        for (Thread filosofo : filosofos) {
            filosofo.join();
        }
        System.out.println("   todos han comido " + BOCADOS + " veces");
    }
}
