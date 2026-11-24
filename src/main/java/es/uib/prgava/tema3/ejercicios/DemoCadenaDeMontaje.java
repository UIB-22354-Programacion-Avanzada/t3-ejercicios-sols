// DemoCadenaDeMontaje.java
package es.uib.prgava.tema3.ejercicios;

import java.util.ArrayList;
import java.util.List;

/**
 * Ejercicio 3.2.5. Las mismas piezas, primero sin cadena —una persona hace los tres pasos de cada
 * pieza, una detrás de otra— y después con la cadena de montaje del ejercicio.
 *
 * <p>Sin argumentos fabrica cuatro piezas; con un número, por ejemplo {@code 10}, fabrica esas.
 * La primera mitad funciona desde el principio, porque solo usa {@link Taller}; la segunda termina
 * con la UnsupportedOperationException del TODO mientras el ejercicio esté sin resolver. Ya está
 * escrito: no hace falta tocarlo.
 */
public final class DemoCadenaDeMontaje {

    private DemoCadenaDeMontaje() { }

    public static void main(String[] args) throws InterruptedException {
        List<String> piezas = new ArrayList<>(List.of("silla", "mesa", "estante", "banco"));
        if (args.length > 0) {
            piezas.clear();
            for (int i = 1; i <= Integer.parseInt(args[0]); i++) {
                piezas.add("pieza " + i);
            }
        }

        long inicio = System.nanoTime();
        for (String pieza : piezas) {
            Taller.empaquetar(Taller.pintar(Taller.ensamblar(pieza)));
        }
        System.out.println("   sin cadena: " + piezas.size() + " piezas en " + desde(inicio) + " ms");

        inicio = System.nanoTime();
        List<String> hechas = CadenaDeMontaje.fabricar(piezas, 2);
        System.out.println("   en cadena:  " + hechas.size() + " piezas en " + desde(inicio) + " ms");
    }

    private static long desde(long inicio) {
        return (System.nanoTime() - inicio) / 1_000_000;
    }
}
