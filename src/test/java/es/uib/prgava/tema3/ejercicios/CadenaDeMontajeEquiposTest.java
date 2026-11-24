package es.uib.prgava.tema3.ejercicios;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Ampliación del ejercicio 3.2.5: más de un trabajador por puesto. */
class CadenaDeMontajeEquiposTest {

    private static List<String> piezas(int n) {
        List<String> todas = new ArrayList<>();
        for (int i = 1; i <= n; i++) {
            todas.add("silla " + i);
        }
        return todas;
    }

    private static List<String> fabricar(List<String> piezas, int capacidad, int e, int p, int k) {
        return assertTimeoutPreemptively(Duration.ofSeconds(10),
                () -> CadenaDeMontaje.fabricar(piezas, capacidad, e, p, k),
                "la cadena de " + e + "-" + p + "-" + k + " no ha terminado en 10 s: faltan pildoras o sobran esperas");
    }

    /** Esta es la que falla si se pierde, se repite o se queda atascada alguna pieza. */
    @Test
    void cadaPiezaSeFabricaUnaSolaVez() {
        Set<String> esperadas = new HashSet<>();
        for (String pieza : piezas(8)) {
            esperadas.add(pieza + ": ensamblada, pintada, empaquetada");
        }
        for (int[] c : new int[][] {{1, 1, 1}, {3, 1, 1}, {1, 3, 1}, {1, 1, 3}, {2, 3, 2}, {4, 4, 4}}) {
            List<String> hechas = fabricar(piezas(8), 2, c[0], c[1], c[2]);
            assertEquals(8, hechas.size(), "piezas terminadas con " + c[0] + "-" + c[1] + "-" + c[2]);
            assertEquals(esperadas, new HashSet<>(hechas), "con " + c[0] + "-" + c[1] + "-" + c[2]);
        }
    }

    @Test
    void casosLimite() {
        assertEquals(List.of(), fabricar(List.of(), 1, 2, 2, 2), "sin piezas");
        assertThrows(IllegalArgumentException.class, () -> CadenaDeMontaje.fabricar(List.of("x"), 1, 0, 1, 1));
        assertThrows(IllegalArgumentException.class, () -> CadenaDeMontaje.fabricar(List.of("x"), 1, 1, 0, 1));
        assertThrows(IllegalArgumentException.class, () -> CadenaDeMontaje.fabricar(List.of("x"), 1, 1, 1, 0));
        assertThrows(IllegalArgumentException.class, () -> CadenaDeMontaje.fabricar(List.of("x"), 0, 1, 1, 1));
    }

    @Test
    void cadaPuestoTieneLosHilosPedidos() {
        Taller.hilosPorOperacion().clear();
        fabricar(piezas(12), 2, 2, 3, 1);
        Set<String> pintores = Taller.hilosPorOperacion().get("pintar");
        assertTrue(Set.of("pintor-1", "pintor-2", "pintor-3").containsAll(pintores),
                "los pintores deben llamarse pintor-1, pintor-2 y pintor-3; han pintado " + pintores);
        assertTrue(pintores.size() > 1, "con tres pintores, solo ha pintado " + pintores);
        assertTrue(Set.of("ensamblador-1", "ensamblador-2").containsAll(Taller.hilosPorOperacion().get("ensamblar")));
        assertEquals(Set.of("empaquetador-1"), Taller.hilosPorOperacion().get("empaquetar"));
    }

    /** Esta es la que falla si los trabajadores de un mismo puesto no trabajan a la vez. */
    @Test
    void masPintoresAcortanLaCadena() {
        long inicio = System.nanoTime();
        fabricar(piezas(10), 3, 2, 3, 1);
        long milis = (System.nanoTime() - inicio) / 1_000_000;
        assertTrue(milis < 800, "10 piezas con 2-3-1 han tardado " + milis
                + " ms; con un trabajador por puesto son unos 1100, y con 2-3-1 unos 500");
    }
}
