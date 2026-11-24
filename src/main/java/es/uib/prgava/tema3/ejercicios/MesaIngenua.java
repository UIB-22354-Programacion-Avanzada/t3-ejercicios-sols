package es.uib.prgava.tema3.ejercicios;

/**
 * Ejercicio 3.1.3. La mesa tal como sale escrita a la primera: <strong>se puede quedar parada</strong>.
 *
 * <p>Cada filósofo toma primero el tenedor de su izquierda y después el de su derecha. No la
 * corrijas: es el código de partida del ejercicio, y la que usa {@code DemoCena}.
 */
public final class MesaIngenua implements Mesa {

    private final Object[] tenedores;

    public MesaIngenua(int comensales) {
        if (comensales < 1) {
            throw new IllegalArgumentException("Hace falta al menos un comensal");
        }
        tenedores = new Object[comensales];
        for (int i = 0; i < comensales; i++) {
            tenedores[i] = new Object();
        }
    }

    @Override
    public int comensales() {
        return tenedores.length;
    }

    @Override
    public void comer(int filosofo, Runnable bocado) {
        Object izquierdo = tenedores[filosofo];
        Object derecho = tenedores[(filosofo + 1) % tenedores.length];
        synchronized (izquierdo) {
            synchronized (derecho) {
                bocado.run();
            }
        }
    }
}
