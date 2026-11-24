package es.uib.prgava.tema3.ejercicios;

/** Ejercicio 3.1.2. El control de aforo de {@link Aforo}, sin la condición de carrera. */
public final class AforoSincronizado implements ControlDeAforo {

    private final int capacidad;
    private int dentro;   // protegido por el cerrojo de this: solo se toca en metodos synchronized

    public AforoSincronizado(int capacidad) {
        if (capacidad < 0) {
            throw new IllegalArgumentException("La capacidad no puede ser negativa");
        }
        this.capacidad = capacidad;
    }

    /**
     * La comprobación y la acción, dentro del mismo cerrojo: mientras un hilo está aquí, ningún
     * otro puede comprobar con un {@code dentro} que está a punto de cambiar.
     */
    @Override
    public synchronized boolean entrar() {
        if (dentro < capacidad) {
            // El mismo yield que en Aforo, para que la comparacion sea justa. Aqui ya no abre
            // ninguna ventana: aunque el hilo ceda el procesador, sigue teniendo el cerrojo.
            Thread.yield();
            dentro++;
            return true;
        }
        return false;
    }

    /**
     * También {@code synchronized}, aunque solo lea: sin el mismo cerrojo, un hilo podría leer
     * un valor antiguo de {@code dentro}, porque nada obliga a que vea lo que escribió otro.
     */
    @Override
    public synchronized int dentro() {
        return dentro;
    }

    /** No necesita {@code synchronized}: es un atributo {@code final}, que no cambia nunca. */
    @Override
    public int capacidad() {
        return capacidad;
    }

    // ----------------------------------------------------------------------------------
    // RESPUESTA al «Para pensar»: cuarenta tornos sin esperarse casi nunca.
    //
    // Repartir el aforo en cupos, uno por torno, cada uno con su propio cerrojo: si caben 40.000
    // personas, cada torno tiene un cupo de 1.000 y solo compite consigo mismo. La suma de los
    // cupos es la capacidad, asi que nunca entra nadie de mas. Cuando un torno agota su cupo, pide
    // prestado a otro que le sobre: esa operacion si necesita coordinar dos cupos, pero es rara.
    // Es la misma idea que LongAdder en 3.2.4, varios contadores en lugar de uno, aplicada a un
    // limite. Un unico AtomicInteger con compareAndSet evitaria el cerrojo, pero los cuarenta
    // tornos seguirian peleando por la misma variable, y con mucho trafico eso tambien se nota.
    // ----------------------------------------------------------------------------------
}
