package es.uib.prgava.tema3.ejercicios;

import java.time.Duration;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/** Ejercicio 3.2.2. Un informe que combina dos consultas asíncronas, con plazo y sin dejarse tumbar. */
public final class EstacionMeteo {

    private EstacionMeteo() { }

    /**
     * El futuro de un informe como {@code "temperatura 21.5, humedad 60"}. Las dos consultas se
     * lanzan a la vez en {@code ejecutor}; la que falle o no conteste dentro de {@code plazo}
     * aparece como {@code ?}. El método vuelve al instante, sin esperar a ninguna.
     */
    public static CompletableFuture<String> informe(Supplier<Double> temperatura,
            Supplier<Integer> humedad, Duration plazo, Executor ejecutor) {
        // Cada futuro se construye entero antes de combinar: lanzar, convertir en texto, poner
        // plazo y poner repuesto. El orden importa: exceptionally va DESPUES de orTimeout para
        // cubrir tambien el fallo por plazo, y despues de thenApply para cubrir sus errores.
        long milis = plazo.toMillis();
        CompletableFuture<String> t = CompletableFuture.supplyAsync(temperatura, ejecutor)
                .thenApply(grados -> String.format(Locale.ROOT, "%.1f", grados))
                .orTimeout(milis, TimeUnit.MILLISECONDS)
                .exceptionally(error -> "?");
        CompletableFuture<String> h = CompletableFuture.supplyAsync(humedad, ejecutor)
                .thenApply(String::valueOf)
                .orTimeout(milis, TimeUnit.MILLISECONDS)
                .exceptionally(error -> "?");
        // Ningun join: el metodo devuelve el futuro del informe y vuelve al instante. Quien llama
        // decide si espera y cuando.
        return t.thenCombine(h, (grados, por100) -> "temperatura " + grados + ", humedad " + por100);
    }

    // ----------------------------------------------------------------------------------
    // RESPUESTA al «Para pensar»: ¿cuando seria un error escribir «?»?
    //
    // Cuando el dato se usa para decidir algo y no solo para mostrarlo. En un panel, un «?» es
    // honesto: dice que no se sabe. Pero si la temperatura alimenta una alarma de incendio, o una
    // factura, o el control de una caldera, sustituir el fallo por un valor de relleno —o, peor,
    // por el ultimo valor conocido sin decirlo— esconde un problema que alguien necesita ver. Ahi
    // lo correcto es que el futuro termine con la excepcion (sin exceptionally), para que quien
    // decide se entere, o devolver el ultimo valor bueno junto con la hora en que se midio, para
    // que la decision sepa cuan viejo es. Repuesto para lo que se muestra; error visible para lo
    // que se decide.
    // ----------------------------------------------------------------------------------
}
