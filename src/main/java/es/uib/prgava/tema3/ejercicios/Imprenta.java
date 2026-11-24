package es.uib.prgava.tema3.ejercicios;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/** Ejercicio 3.2.4. Varias oficinas mandan documentos a una cola, y varias impresoras los imprimen. */
public final class Imprenta {

    private static final String FIN = "fin de la jornada";

    private Imprenta() { }

    /** Imprimir un documento cuesta 10 ms. Ya está escrito. */
    static void imprimirUno(String documento) throws InterruptedException {
        Thread.sleep(10);
    }

    /**
     * Una oficina por cada lista de {@code oficinas} y {@code impresoras} impresoras, comunicadas
     * por una cola de {@code capacidad} documentos. Vuelve cuando se ha impreso todo, con lo que ha
     * impreso cada impresora, por su nombre: {@code "impresora-1"}, {@code "impresora-2"}…
     */
    public static Map<String, List<String>> imprimir(List<List<String>> oficinas, int impresoras,
            int capacidad) throws InterruptedException {
        if (impresoras < 1 || capacidad < 1) {
            throw new IllegalArgumentException("Hace falta al menos una impresora y sitio en la cola");
        }
        BlockingQueue<String> cola = new ArrayBlockingQueue<>(capacidad);

        List<Thread> productores = new ArrayList<>();
        for (List<String> documentos : oficinas) {
            productores.add(Thread.ofPlatform().start(() -> {
                try {
                    for (String documento : documentos) {
                        cola.put(documento);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }));
        }

        Map<String, List<String>> impresos = new LinkedHashMap<>();
        List<Thread> consumidores = new ArrayList<>();
        for (int i = 1; i <= impresoras; i++) {
            // Una lista por impresora, que solo toca su hilo: no hay nada compartido que proteger, y
            // el mapa se lee despues del join de todas.
            List<String> mios = new ArrayList<>();
            String nombre = "impresora-" + i;
            impresos.put(nombre, mios);
            consumidores.add(Thread.ofPlatform().name(nombre).start(() -> {
                try {
                    while (true) {
                        String documento = cola.take();
                        if (documento.equals(FIN)) {
                            return;
                        }
                        imprimirUno(documento);
                        mios.add(documento);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }));
        }

        // Ninguna oficina sabe si las demas han terminado: quien lo sabe es este metodo, despues de
        // esperarlas a todas. Solo entonces pone las pildoras, y una por impresora, porque cada
        // impresora que saca una termina y deja de sacar. Ponerlas antes de los join haria que
        // alguna impresora parara con documentos todavia por llegar.
        for (Thread productor : productores) {
            productor.join();
        }
        for (int i = 0; i < impresoras; i++) {
            cola.put(FIN);
        }
        for (Thread consumidor : consumidores) {
            consumidor.join();
        }
        return impresos;
    }

    // ----------------------------------------------------------------------------------
    // RESPUESTA al «Para pensar»: que ninguna oficina acapare las impresoras.
    //
    // Con una sola cola, el orden es el de llegada: si una oficina mete mil documentos de golpe,
    // los de las demas esperan detras de los mil. Para repartir de forma justa hacen falta colas
    // separadas, una por oficina, y que las impresoras las atiendan por turno: un documento de la
    // primera, uno de la segunda, y asi (round-robin), saltando las vacias. Es lo que hacen los
    // planificadores de los sistemas operativos y de los routers con varias colas. Una alternativa
    // mas sencilla es limitar cuantos documentos puede tener cada oficina en la cola a la vez, con
    // un semaforo por oficina: no reparte perfecto, pero impide que una sola lo llene todo.
    // ----------------------------------------------------------------------------------
}
