package es.uib.prgava.tema3.ejercicios;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Ejercicio 3.2.5. Una cadena de montaje de tres puestos —ensamblar, pintar y empaquetar—
 * comunicados por colas.
 *
 * <p>Cada puesto es un hilo. Entre dos puestos seguidos hay una cola bloqueante: el puesto de la
 * izquierda deja en ella las piezas que termina (es el productor) y el de la derecha las saca para
 * seguir trabajando (es el consumidor). Así los tres puestos trabajan a la vez sobre piezas
 * distintas, y ninguno necesita saber nada de los otros: solo de su cola de entrada y de su cola
 * de salida.
 */
public final class CadenaDeMontaje {

    /**
     * La píldora venenosa: una pieza especial que no se fabrica, sino que avisa de que no van a
     * llegar más. Cuando un puesto la saca de su cola, sabe que ha terminado.
     */
    private static final String FIN = "fin de la jornada";

    private CadenaDeMontaje() { }

    /**
     * Fabrica las piezas con un hilo por puesto, {@code "ensamblador"}, {@code "pintor"} y
     * {@code "empaquetador"}, y colas de {@code capacidad} piezas entre ellos. Devuelve las piezas
     * terminadas en el orden en que entraron.
     *
     * <pre>
     * piezas -> [ensamblador] -> ensambladas -> [pintor] -> pintadas -> [empaquetador] -> terminadas
     * </pre>
     */
    public static List<String> fabricar(List<String> piezas, int capacidad) throws InterruptedException {
        if (capacidad < 1) {
            throw new IllegalArgumentException("Las colas necesitan sitio para al menos una pieza");
        }

        // Las dos colas entre puestos. Son ArrayBlockingQueue de tamano fijo, asi que:
        //  - put espera si la cola esta llena: un puesto rapido no puede adelantarse demasiado
        //    al siguiente, y las piezas a medio hacer nunca pasan de 'capacidad';
        //  - take espera si la cola esta vacia: un puesto no hace nada hasta que le llega trabajo.
        // No hace falta una tercera cola: el ensamblador lee directamente la lista de piezas, y el
        // empaquetador guarda las terminadas en una lista.
        BlockingQueue<String> ensambladas = new ArrayBlockingQueue<>(capacidad);
        BlockingQueue<String> pintadas = new ArrayBlockingQueue<>(capacidad);

        // Una ArrayList corriente, no una coleccion concurrente: solo la toca el hilo empaquetador,
        // y este metodo no la lee hasta despues del join (ver al final).
        List<String> terminadas = new ArrayList<>();

        // Puesto 1. Es solo productor: recorre la lista, ensambla cada pieza y la deja en la cola.
        // new Thread(tarea, nombre) crea el hilo sin arrancarlo; la tarea es la lambda.
        Thread ensamblador = new Thread(() -> {
            try {
                for (String pieza : piezas) {
                    ensambladas.put(Taller.ensamblar(pieza));   // espera si 'ensambladas' esta llena
                }
                ensambladas.put(FIN);   // no quedan piezas: avisa al pintor por su cola
            } catch (InterruptedException e) {
                // Si alguien interrumpe al hilo mientras espera en put, se restaura la marca de
                // interrupcion y se termina, en lugar de tragarse la excepcion.
                Thread.currentThread().interrupt();
            }
        }, "ensamblador");

        // Puesto 2. Es consumidor de 'ensambladas' y productor de 'pintadas'.
        Thread pintor = new Thread(() -> {
            try {
                while (true) {
                    String pieza = ensambladas.take();          // espera si 'ensambladas' esta vacia
                    if (pieza.equals(FIN)) {
                        // El empaquetador no ve la cola 'ensambladas': solo se enterara del final
                        // si el pintor le pasa la pildora por la suya. Sin esta linea, el
                        // empaquetador se quedaria esperando en take para siempre.
                        pintadas.put(FIN);
                        return;
                    }
                    pintadas.put(Taller.pintar(pieza));         // espera si 'pintadas' esta llena
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "pintor");

        // Puesto 3. Es solo consumidor: saca piezas de 'pintadas' y las guarda terminadas.
        Thread empaquetador = new Thread(() -> {
            try {
                while (true) {
                    String pieza = pintadas.take();             // espera si 'pintadas' esta vacia
                    if (pieza.equals(FIN)) {
                        return;     // es el ultimo puesto: no hay nadie a quien pasar la pildora
                    }
                    terminadas.add(Taller.empaquetar(pieza));
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "empaquetador");

        // Primero se arrancan los tres, para que trabajen a la vez...
        ensamblador.start();
        pintor.start();
        empaquetador.start();

        // ...y despues se espera a cada uno. Si hicieramos start y join de cada hilo seguidos, el
        // pintor no empezaria hasta que el ensamblador hubiera terminado, y con colas pequenas el
        // ensamblador se quedaria esperando sitio para siempre.
        ensamblador.join();
        pintor.join();
        empaquetador.join();

        // join garantiza que todo lo que hizo el empaquetador es visible desde aqui: se puede leer
        // 'terminadas' sin sincronizar. El orden de entrada se conserva solo: cada puesto tiene un
        // unico hilo y las colas sacan las piezas en el orden en que entraron (FIFO).
        return terminadas;
    }

    /**
     * Ampliación. Fabrica las piezas con varios hilos en cada puesto, llamados
     * {@code "ensamblador-1"}, {@code "pintor-1"}, {@code "empaquetador-1"}… Las piezas terminadas
     * salen en cualquier orden: dos pintores no terminan necesariamente en el orden en que
     * empezaron.
     */
    public static List<String> fabricar(List<String> piezas, int capacidad,
            int ensambladores, int pintores, int empaquetadores) throws InterruptedException {
        if (capacidad < 1 || ensambladores < 1 || pintores < 1 || empaquetadores < 1) {
            throw new IllegalArgumentException("Hace falta sitio en las colas y al menos un trabajador por puesto");
        }

        // Con varios ensambladores, la lista de piezas tiene que repartirse. Lo mas sencillo es
        // meterlas en una cola: cada ensamblador saca la siguiente que quede, y nunca dos la misma.
        // Detras van las pildoras, una por ensamblador, porque cada uno tiene que sacar la suya.
        // Esta cola se crea con sitio para todo, asi que estos put nunca esperan.
        BlockingQueue<String> pendientes = new ArrayBlockingQueue<>(piezas.size() + ensambladores);
        for (String pieza : piezas) {
            pendientes.put(pieza);
        }
        for (int i = 0; i < ensambladores; i++) {
            pendientes.put(FIN);
        }

        // Las colas entre puestos, igual que en la version de un trabajador por puesto.
        BlockingQueue<String> ensambladas = new ArrayBlockingQueue<>(capacidad);
        BlockingQueue<String> pintadas = new ArrayBlockingQueue<>(capacidad);

        // Puesto 1: los ensambladores. Cada vuelta del for crea un hilo y lo arranca enseguida:
        // no hace falta tener el equipo completo para empezar a trabajar. El nombre se calcula
        // fuera de la lambda, con el i de esa vuelta.
        List<Thread> puesto1 = new ArrayList<>();
        for (int i = 1; i <= ensambladores; i++) {
            Thread hilo = new Thread(() -> {
                try {
                    while (true) {
                        String pieza = pendientes.take();
                        if (pieza.equals(FIN)) {
                            return;     // su pildora: este ensamblador ha terminado
                        }
                        ensambladas.put(Taller.ensamblar(pieza));
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, "ensamblador-" + i);
            puesto1.add(hilo);
            hilo.start();
        }

        // Puesto 2: los pintores. A diferencia del pintor de la version anterior, NO pasan la
        // pildora al puesto siguiente. Si el primer pintor que la recibe la pasara, los
        // empaquetadores podrian terminar mientras otro pintor todavia tiene una pieza entre manos,
        // y esa pieza se perderia. Las pildoras de cada puesto las pone el metodo (ver al final).
        List<Thread> puesto2 = new ArrayList<>();
        for (int i = 1; i <= pintores; i++) {
            Thread hilo = new Thread(() -> {
                try {
                    while (true) {
                        String pieza = ensambladas.take();
                        if (pieza.equals(FIN)) {
                            return;
                        }
                        pintadas.put(Taller.pintar(pieza));
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, "pintor-" + i);
            puesto2.add(hilo);
            hilo.start();
        }

        // Puesto 3: los empaquetadores. Cada uno guarda lo suyo en su propia lista, 'suyas', que
        // se crea en cada vuelta del for: asi cada lambda captura una lista distinta y ningun par
        // de hilos escribe en la misma. Se juntan al final, cuando ya han terminado todos.
        List<Thread> puesto3 = new ArrayList<>();
        List<List<String>> listas = new ArrayList<>();
        for (int i = 1; i <= empaquetadores; i++) {
            List<String> suyas = new ArrayList<>();
            listas.add(suyas);
            Thread hilo = new Thread(() -> {
                try {
                    while (true) {
                        String pieza = pintadas.take();
                        if (pieza.equals(FIN)) {
                            return;
                        }
                        suyas.add(Taller.empaquetar(pieza));
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, "empaquetador-" + i);
            puesto3.add(hilo);
            hilo.start();
        }

        // El final, puesto a puesto y en este orden:
        //  1. esperar a que terminen TODOS los trabajadores de un puesto;
        //  2. solo entonces, dejar en la cola del puesto siguiente una pildora por trabajador
        //     de ese puesto, porque cada uno necesita la suya para terminar.
        // Es lo mismo que hace la Imprenta del 3.2.4, repetido en cada puesto.
        for (Thread hilo : puesto1) {
            hilo.join();
        }
        for (int i = 0; i < pintores; i++) {
            ensambladas.put(FIN);   // detras de la ultima pieza ensamblada: no adelanta a ninguna
        }
        for (Thread hilo : puesto2) {
            hilo.join();
        }
        for (int i = 0; i < empaquetadores; i++) {
            pintadas.put(FIN);
        }
        for (Thread hilo : puesto3) {
            hilo.join();
        }

        // Todos han terminado: se juntan las listas de los empaquetadores en una sola.
        List<String> terminadas = new ArrayList<>();
        for (List<String> suyas : listas) {
            terminadas.addAll(suyas);
        }
        return terminadas;
    }

    // ----------------------------------------------------------------------------------
    // RESPUESTA al «Para pensar»: ¿de que puesto depende el tiempo?
    //
    // Del mas lento, el pintor, con 100 ms por pieza: es el cuello de botella, y la cadena saca
    // una pieza terminada cada 100 ms en regimen. La primera tarda lo que los tres pasos, 60 + 100
    // + 30 = 190 ms; cada una de las demas sale 100 ms despues. Con n piezas,
    // T(n) = 60 + 100n + 30 = 100n + 90 ms: 490 ms para cuatro (se midieron unos 520) y 1.090 para
    // diez (se midieron 1.097). Lo que sobra es el arranque de los hilos y la impresion. En
    // general, T(n) = suma de los tiempos + (n - 1) * maximo de los tiempos.
    // ----------------------------------------------------------------------------------
}
