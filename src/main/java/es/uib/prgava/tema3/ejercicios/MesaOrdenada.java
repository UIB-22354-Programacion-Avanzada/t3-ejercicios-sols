package es.uib.prgava.tema3.ejercicios;

/** Ejercicio 3.1.3. La mesa sin interbloqueo: los tenedores se toman en un orden global. */
public final class MesaOrdenada implements Mesa {

    private final Object[] tenedores;

    public MesaOrdenada(int comensales) {
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
        if (filosofo < 0 || filosofo >= tenedores.length) {
            throw new IllegalArgumentException("No hay filosofo " + filosofo);
        }
        int izquierdo = filosofo;
        int derecho = (filosofo + 1) % tenedores.length;
        int primero = Math.min(izquierdo, derecho);
        int segundo = Math.max(izquierdo, derecho);
        synchronized (tenedores[primero]) {
            synchronized (tenedores[segundo]) {
                bocado.run();
            }
        }
    }

    // ----------------------------------------------------------------------------------
    // COFFMAN. La mesa ingenua cumple las cuatro condiciones: los tenedores son exclusivos, cada
    // filosofo retiene el de su izquierda mientras espera el de su derecha, nadie le quita un
    // tenedor a otro, y si los cinco toman el izquierdo a la vez se forma el ciclo 0 -> 1 -> 2 ->
    // 3 -> 4 -> 0. Esta mesa rompe la cuarta, la ESPERA CIRCULAR. Todos toman los tenedores en
    // orden creciente de numero, y en un ciclo de esperas cada filosofo esperaria un tenedor de
    // numero mayor que el que tiene; recorriendo el ciclo se volveria al primero con un numero
    // mayor que el suyo, lo que es imposible. Sin ciclo no hay interbloqueo.
    //
    // RESPUESTA al «Para pensar»: el filosofo zurdo es la MISMA solucion. Para los filosofos 0 a
    // n-2, el tenedor de la izquierda (i) tiene numero menor que el de la derecha (i+1), y el orden
    // global coincide con «izquierda primero». Solo el filosofo n-1 tiene a su derecha el tenedor 0,
    // de numero menor, y por tanto lo toma primero: es exactamente el zurdo. Las dos rompen la
    // espera circular. Lo que el orden global aporta es la generalidad: la regla «de menor a mayor»
    // funciona igual con cualquier conjunto de cerrojos, no solo en una mesa redonda.
    // ----------------------------------------------------------------------------------
}
