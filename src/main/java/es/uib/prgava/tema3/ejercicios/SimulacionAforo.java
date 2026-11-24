package es.uib.prgava.tema3.ejercicios;

/** Ejercicio 3.1.2. Una simulación que pone a prueba un control de aforo con varios hilos. */
public final class SimulacionAforo {

    private SimulacionAforo() { }

    /**
     * Lanza {@code hilos} hilos que intentan entrar {@code intentosPorHilo} veces cada uno, espera a
     * que terminen y devuelve cuántas entradas fueron aceptadas en total.
     */
    public static int entradasAceptadas(ControlDeAforo aforo, int hilos, int intentosPorHilo)
            throws InterruptedException {
        if (hilos < 1 || intentosPorHilo < 0) {
            throw new IllegalArgumentException("Hace falta al menos un hilo y un numero de intentos no negativo");
        }
        // Una casilla por hilo: cada hilo escribe solo en la suya y nadie lee hasta despues de los
        // join, asi que no hay ningun dato compartido que proteger. Una variable compartida para
        // el total tendria su propia condicion de carrera, la misma que se quiere demostrar.
        int[] aceptadas = new int[hilos];
        Thread[] lanzados = new Thread[hilos];
        for (int i = 0; i < hilos; i++) {
            int mio = i;   // la lambda solo puede capturar variables locales que no cambian
            lanzados[i] = new Thread(() -> {
                for (int k = 0; k < intentosPorHilo; k++) {
                    if (aforo.entrar()) {
                        aceptadas[mio]++;
                    }
                }
            });
        }
        for (Thread hilo : lanzados) {
            hilo.start();
        }
        // join no solo espera: garantiza que lo que escribio el hilo es visible para el que espera.
        // Por eso leer aceptadas[i] despues del join es seguro sin synchronized.
        int total = 0;
        for (int i = 0; i < hilos; i++) {
            lanzados[i].join();
            total += aceptadas[i];
        }
        return total;
    }

    /** La demostración del paso 4: la carrera de {@code Aforo} y su arreglo, uno detrás de otro. */
    public static void main(String[] args) throws InterruptedException {
        int conAforo = entradasAceptadas(new Aforo(1000), 8, 500);
        int conSincronizado = entradasAceptadas(new AforoSincronizado(1000), 8, 500);
        System.out.println("Aforo:             " + conAforo + " entradas para 1000 plazas");
        System.out.println("AforoSincronizado: " + conSincronizado + " entradas para 1000 plazas");
    }
}
