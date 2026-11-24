package es.uib.prgava.tema3.ejercicios;

/**
 * Ejercicio 3.1.3. Una mesa redonda de filósofos, con un tenedor entre cada dos.
 *
 * <p>El filósofo {@code i} come con el tenedor {@code i}, a su izquierda, y el tenedor
 * {@code (i + 1) % comensales()}, a su derecha. Para comer necesita los dos.
 */
public interface Mesa {

    /** Cuántos filósofos, y cuántos tenedores, hay en la mesa. */
    int comensales();

    /** El filósofo {@code filosofo} toma sus dos tenedores, ejecuta {@code bocado} y los suelta. */
    void comer(int filosofo, Runnable bocado);
}
