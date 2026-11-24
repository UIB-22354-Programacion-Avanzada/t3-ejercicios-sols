package es.uib.prgava.tema3.ejercicios;

/**
 * Ejercicio 3.1.2. Un control de aforo <strong>con una condición de carrera</strong>.
 *
 * <p>Con un solo hilo es correcto. Con varios, deja entrar a más gente de la que cabe. No lo
 * corrijas: es el código de partida del ejercicio, y el que la simulación tiene que pillar en falta.
 */
public final class Aforo implements ControlDeAforo {

    private final int capacidad;
    private int dentro;

    public Aforo(int capacidad) {
        if (capacidad < 0) {
            throw new IllegalArgumentException("La capacidad no puede ser negativa");
        }
        this.capacidad = capacidad;
    }

    @Override
    public boolean entrar() {
        if (dentro < capacidad) {       // comprobar...
            Thread.yield();             // (cede el procesador: hace la carrera más visible)
            dentro++;                   // ...y despues actuar
            return true;
        }
        return false;
    }

    @Override
    public int dentro() {
        return dentro;
    }

    @Override
    public int capacidad() {
        return capacidad;
    }
}
