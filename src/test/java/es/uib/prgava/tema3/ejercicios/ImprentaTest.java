package es.uib.prgava.tema3.ejercicios;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Ejercicio 3.2.4. */
class ImprentaTest {

    private static List<List<String>> oficinas(int cuantas, int documentos) {
        List<List<String>> todas = new ArrayList<>();
        for (int o = 1; o <= cuantas; o++) {
            List<String> suyos = new ArrayList<>();
            for (int d = 1; d <= documentos; d++) {
                suyos.add("oficina" + o + "-doc" + d);
            }
            todas.add(suyos);
        }
        return todas;
    }

    private static Map<String, List<String>> imprimir(List<List<String>> oficinas, int impresoras, int capacidad) {
        return assertTimeoutPreemptively(Duration.ofSeconds(10), () -> Imprenta.imprimir(oficinas, impresoras, capacidad),
                "la imprenta no ha terminado en 10 s: alguna impresora o alguna oficina se ha quedado esperando");
    }

    @Test
    void cadaImpresoraApareceConSuNombre() {
        Map<String, List<String>> impresos = imprimir(oficinas(2, 5), 3, 4);
        assertEquals(Set.of("impresora-1", "impresora-2", "impresora-3"), impresos.keySet());
    }

    /** Esta es la que falla si se pierde o se repite algún documento. */
    @Test
    void seImprimeCadaDocumentoUnaSolaVez() {
        for (int[] caso : new int[][] {{1, 1, 1}, {3, 1, 2}, {1, 4, 1}, {4, 3, 2}, {5, 2, 10}}) {
            List<List<String>> todas = oficinas(caso[0], 12);
            Map<String, List<String>> impresos = imprimir(todas, caso[1], caso[2]);
            List<String> juntos = new ArrayList<>();
            impresos.values().forEach(juntos::addAll);
            Set<String> esperados = new HashSet<>();
            todas.forEach(esperados::addAll);
            assertEquals(esperados.size(), juntos.size(), "documentos impresos con " + caso[0] + " oficinas y " + caso[1] + " impresoras");
            assertEquals(esperados, new HashSet<>(juntos));
        }
    }

    @Test
    void sinDocumentosTambienTermina() {
        Map<String, List<String>> impresos = imprimir(List.of(List.of(), List.of()), 2, 1);
        assertEquals(List.of(), impresos.get("impresora-1"));
        assertEquals(List.of(), impresos.get("impresora-2"));
    }

    @Test
    void conUnaSolaImpresoraSeRespetaElOrdenDeCadaOficina() {
        Map<String, List<String>> impresos = imprimir(oficinas(3, 10), 1, 2);
        List<String> impresora = impresos.get("impresora-1");
        for (int o = 1; o <= 3; o++) {
            String prefijo = "oficina" + o + "-";
            List<String> suyos = impresora.stream().filter(d -> d.startsWith(prefijo)).toList();
            assertEquals(oficinas(3, 10).get(o - 1), suyos, "orden de la oficina " + o);
        }
    }

    @Test
    void lasImpresorasTrabajanALaVez() {
        long inicio = System.nanoTime();
        imprimir(oficinas(2, 20), 4, 8);
        long milis = (System.nanoTime() - inicio) / 1_000_000;
        assertTrue(milis < 300, "40 documentos de 10 ms con 4 impresoras han tardado " + milis + " ms");
    }

    @Test
    void losArgumentosSinSentidoSonRechazados() {
        assertThrows(IllegalArgumentException.class, () -> Imprenta.imprimir(oficinas(1, 1), 0, 5));
        assertThrows(IllegalArgumentException.class, () -> Imprenta.imprimir(oficinas(1, 1), 2, 0));
    }
}
