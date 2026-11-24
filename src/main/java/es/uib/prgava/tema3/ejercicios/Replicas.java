package es.uib.prgava.tema3.ejercicios;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.StructuredTaskScope.Joiner;

/** Ejercicio 3.3.2. Preguntar a varias réplicas a la vez y quedarse con la primera que conteste bien. */
public final class Replicas {

    private Replicas() { }

    /**
     * La primera respuesta correcta de las réplicas, consultadas a la vez en un ámbito
     * estructurado con plazo. Cuando vuelve, ninguna consulta sigue en marcha.
     *
     * @throws StructuredTaskScope.FailedException si fallan todas
     * @throws StructuredTaskScope.TimeoutException si ninguna contesta bien dentro del plazo
     */
    public static <T> T primeraRespuesta(List<Callable<T>> replicas, Duration plazo)
            throws InterruptedException {
        if (replicas.isEmpty()) {
            throw new IllegalArgumentException("Hace falta al menos una replica");
        }
        // anySuccessfulResultOrThrow: con el primer exito, el ambito cancela las demas y join lo
        // devuelve; si fallan todas, join lanza FailedException; si vence el plazo, TimeoutException.
        // Y el close del try espera a que las canceladas terminen: al volver, no queda ninguna viva.
        try (var ambito = StructuredTaskScope.open(Joiner.<T>anySuccessfulResultOrThrow(),
                configuracion -> configuracion.withTimeout(plazo))) {
            for (Callable<T> replica : replicas) {
                ambito.fork(replica);
            }
            return ambito.join();
        }
    }

    // ----------------------------------------------------------------------------------
    // RESPUESTA al «Para pensar»: ¿cuando merece la pena triplicar la carga?
    //
    // Cuando la latencia importa mas que la capacidad: consultas de lectura, idempotentes y
    // baratas, en un servicio con capacidad de sobra y una cola de latencias larga —la mayoria de
    // las respuestas llegan rapido, pero de vez en cuando una replica tarda mucho—. Para
    // escrituras, o consultas caras, o un servicio cerca de su limite, triplicar la carga lo hace
    // todo mas lento. La variante que paga menos es la PETICION DE RESPALDO (hedged request):
    // preguntar a una replica y, solo si no contesta en un tiempo —por ejemplo, el percentil 95 de
    // lo normal—, preguntar a otra y quedarse con la primera respuesta. Casi todas las consultas
    // cuestan una peticion, y las lentas se rescatan con una segunda.
    // ----------------------------------------------------------------------------------
}
