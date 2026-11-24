package es.uib.prgava.tema3.ejercicios;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Ejercicio 3.2.5. */
class CadenaDeMontajeTest {

    private static List<String> piezas(int n) {
        List<String> todas = new ArrayList<>();
        for (int i = 1; i <= n; i++) {
            todas.add("silla " + i);
        }
        return todas;
    }

    private static String terminada(String pieza) {
        return pieza + ": ensamblada, pintada, empaquetada";
    }

    private static List<String> fabricar(List<String> piezas, int capacidad) {
        return assertTimeoutPreemptively(Duration.ofSeconds(10), () -> CadenaDeMontaje.fabricar(piezas, capacidad),
                "la cadena no ha terminado en 10 s: algun puesto se ha quedado esperando una pieza o la pildora");
    }

    @Test
    void cadaPiezaPasaPorLosTresPasosEnOrden() {
        assertEquals(List.of(terminada("silla"), terminada("mesa"), terminada("estante")),
                fabricar(List.of("silla", "mesa", "estante"), 2));
    }

    @Test
    void conservaElOrdenDeEntrada() {
        List<String> esperadas = piezas(12).stream().map(CadenaDeMontajeTest::terminada).toList();
        assertEquals(esperadas, fabricar(piezas(12), 1));
    }

    @Test
    void casosLimite() {
        assertEquals(List.of(), fabricar(List.of(), 3), "sin piezas");
        assertEquals(List.of(terminada("x")), fabricar(List.of("x"), 1), "con una sola pieza");
        assertThrows(IllegalArgumentException.class, () -> CadenaDeMontaje.fabricar(List.of("x"), 0));
    }

    @Test
    void cadaPasoLoHaceElHiloDeSuPuesto() {
        Taller.hilosPorOperacion().clear();
        fabricar(piezas(5), 2);
        Map<String, Set<String>> hilos = Taller.hilosPorOperacion();
        assertEquals(Set.of("ensamblador"), hilos.get("ensamblar"), "hilos que han ensamblado");
        assertEquals(Set.of("pintor"), hilos.get("pintar"), "hilos que han pintado");
        assertEquals(Set.of("empaquetador"), hilos.get("empaquetar"), "hilos que han empaquetado");
    }

    /** Esta es la que falla si los puestos no trabajan a la vez. */
    @Test
    void losPuestosTrabajanALaVez() {
        long inicio = System.nanoTime();
        fabricar(piezas(10), 2);
        long milis = (System.nanoTime() - inicio) / 1_000_000;
        assertTrue(milis < 1500, "10 piezas han tardado " + milis
                + " ms; una detras de otra serian 1900, en cadena unos 1100");
    }
}
