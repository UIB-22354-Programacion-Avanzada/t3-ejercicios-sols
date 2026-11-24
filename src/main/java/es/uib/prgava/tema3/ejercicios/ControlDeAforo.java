package es.uib.prgava.tema3.ejercicios;

/** Ejercicio 3.1.2. Un control de aforo: deja entrar mientras quede sitio. */
public interface ControlDeAforo {

    /** Intenta entrar: si queda sitio, ocupa una plaza y devuelve {@code true}; si no, {@code false}. */
    boolean entrar();

    /** Cuántas personas hay dentro. */
    int dentro();

    /** Cuántas personas caben. */
    int capacidad();
}
