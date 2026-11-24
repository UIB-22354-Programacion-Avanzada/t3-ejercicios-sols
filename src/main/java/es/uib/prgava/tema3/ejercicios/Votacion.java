package es.uib.prgava.tema3.ejercicios;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/** Ejercicio 3.2.3. Un recuento de votos que admite votar desde muchos hilos a la vez. */
public final class Votacion {

    private final ConcurrentHashMap<String, Long> votos = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> votantes = new ConcurrentHashMap<>();
    private final AtomicLong total = new AtomicLong();

    /** Suma un voto a {@code opcion}. */
    public void votar(String opcion) {
        // merge lee, suma y escribe de forma atomica para esa clave; get y put por separado
        // perderian votos cuando dos hilos votan lo mismo a la vez.
        votos.merge(opcion, 1L, Long::sum);
        total.incrementAndGet();
    }

    /**
     * Suma un voto a {@code opcion} si {@code votante} no ha votado todavía, y devuelve si lo ha
     * sumado. Un votante solo vota una vez, aunque lo intente desde varios hilos a la vez.
     */
    public boolean votarUnaVez(String votante, String opcion) {
        // putIfAbsent comprueba e inserta en una sola operacion atomica: de varios hilos que lo
        // intentan con el mismo votante, exactamente uno recibe null. containsKey seguido de put
        // dejaria una ventana en la que dos hilos ven «no ha votado» y votan los dos.
        if (votantes.putIfAbsent(votante, opcion) != null) {
            return false;
        }
        votar(opcion);
        return true;
    }

    /** Los votos de {@code opcion}, o 0 si no tiene ninguno. */
    public long votos(String opcion) {
        return votos.getOrDefault(opcion, 0L);
    }

    /** Los votos emitidos, de todas las opciones. */
    public long total() {
        return total.get();
    }

    /** Una copia de los resultados en este momento. */
    public Map<String, Long> resultados() {
        return Map.copyOf(votos);
    }

    // ----------------------------------------------------------------------------------
    // RESPUESTA al «Para pensar»: ¿pueden no coincidir total() y la suma de resultados()?
    //
    // Si. votar hace dos operaciones atomicas, el merge y el incrementAndGet, pero el par no es
    // atomico: entre las dos, otro hilo puede leer el total y el mapa y ver un voto en uno y no en
    // el otro. Y resultados() copia el mapa clave a clave mientras se sigue votando, asi que ni
    // siquiera la copia es una foto de un instante. Para un marcador que se refresca cada segundo
    // no importa: la diferencia dura microsegundos. Si importara —un escrutinio oficial—, hay dos
    // salidas: calcular el total sumando el mapa en lugar de guardarlo aparte (un solo dato, una
    // sola verdad, a cambio de un recorrido por cada consulta), o proteger votar y las lecturas
    // con un mismo cerrojo, que da coherencia total a cambio de que los votos entren de uno en uno.
    // ----------------------------------------------------------------------------------
}
