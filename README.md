# t3-ejercicios-sols — Soluciones del Tema 3

Soluciones comentadas de los **10 ejercicios** del Tema 3 de *Programación Avanzada (22354)*,
Grado en Ingeniería Telemática, UIB-EPS.

> ⚠️ **Repositorio privado, uso docente.** Es la contrapartida de
> [`t3-ejercicios`](https://github.com/UIB-22354-Programacion-Avanzada/t3-ejercicios), que es el
> esqueleto que resuelve el alumnado. No debe hacerse público mientras los ejercicios estén en
> uso: los enunciados son la base de los Talleres y del Examen Parcial.

## Qué tiene de distinto

Mismo proyecto Maven, mismas clases, mismas pruebas, pero **todo resuelto y todas las pruebas en
verde**, incluida la ampliación de 3.2.5. Y, sobre todo, **comentado**: cada solución explica no
solo qué hace el código, sino por qué se ha elegido esa forma y qué habría pasado con las
alternativas.

Los comentarios son de tres tipos:

- **Javadoc**: la decisión de fondo, por ejemplo por qué `dentro()` también es `synchronized`
  aunque solo lea.
- **Comentarios de línea**: el detalle que se pasa por alto, como los dos bucles de `start` y
  `join`, el `release` en un `finally`, el orden de `orTimeout` y `exceptionally`, o por qué las
  píldoras se ponen después de esperar a los productores.
- **Bloques de respuesta al final de la clase**: la respuesta a la pregunta de **«Para pensar»**,
  que no está en el tema y es exactamente el tipo de pregunta del Examen Parcial.

Las soluciones usan la construcción más sencilla que resuelve cada ejercicio: `new Thread` y
`join` donde basta, un ejecutor donde hay que repartir tareas, y concurrencia estructurada solo en
3.3.2. `CadenaDeMontaje` es la versión comentada paso a paso que se usa en clase.

## Comprobación

```bash
mvn test
```

Debe dar **57 pruebas, todas en verde**: las 53 de la plantilla más las 4 de
`CadenaDeMontajeEquiposTest` (la ampliación), que aquí no están desactivadas. Si alguna falla, la
solución y su prueba se han desincronizado: es el primer sitio donde mirar antes de tocar un
enunciado.

Las pruebas de concurrencia repiten cada escenario muchas veces, pero una prueba en verde no
demuestra que no haya carreras. Si alguna falla de vez en cuando, es un error de verdad, no mala
suerte.

Además hay cuatro programas que conviene ejecutar al menos una vez, porque su **salida** es parte
de la explicación:

```bash
mvn -q compile exec:java -Dexec.mainClass=es.uib.prgava.tema3.ejercicios.DemoCreadorDeHilos   # 3.1.1
mvn -q compile exec:java -Dexec.mainClass=es.uib.prgava.tema3.ejercicios.SimulacionAforo      # 3.1.2
mvn -q compile exec:java -Dexec.mainClass=es.uib.prgava.tema3.ejercicios.DemoCena             # 3.1.3
mvn -q compile exec:java -Dexec.mainClass=es.uib.prgava.tema3.ejercicios.DemoCadenaDeMontaje -Dexec.args="10"  # 3.2.5
```

`SimulacionAforo` enfrenta `Aforo` y `AforoSincronizado` con 8 hilos y 500 intentos cada uno: el
primero suele dejar entrar a más de 1.000 personas y el segundo, nunca.

## Dónde está cada ejercicio

| Ejercicio | Clases |
|---|---|
| 3.1.1 | `CreadorDeHilos` |
| 3.1.2 | `AforoSincronizado`, `SimulacionAforo` |
| 3.1.3 | `MesaOrdenada` |
| 3.2.1 | `PrimosEnParalelo` |
| 3.2.2 | `EstacionMeteo` |
| 3.2.3 | `Votacion` |
| 3.2.4 | `Imprenta` |
| 3.2.5 | `CadenaDeMontaje` (con la ampliación por equipos) |
| 3.3.1 | `Descargador` |
| 3.3.2 | `Replicas` |

`Aforo`, `ControlDeAforo`, `Mesa`, `MesaIngenua`, `Taller` y los programas `Demo*` son copia
exacta de la plantilla.

## Diferencias respecto a la plantilla

Lo que hay aquí y no en `t3-ejercicios`:

- los cuerpos de todos los métodos, comentados;
- el `main` de `SimulacionAforo`;
- los bloques de respuesta al final de cada clase;
- en `CadenaDeMontajeEquiposTest`, la prueba de la ampliación sin el `@Disabled`;
- el `artifactId` del `pom.xml`, que pasa a `t3-ejercicios-sols`.

Nada de lo demás cambia: `.mvn/jvm.config`, la opción `--enable-preview` y el resto del `pom.xml`
son los mismos, de modo que un `diff -r` entre los dos repositorios muestra exactamente el trabajo
del alumnado.

## Cómo usarlo en clase

Las soluciones no están pensadas para entregarse, sino para **corregir y discutir**. Los bloques de
respuesta al final de cada clase están escritos como se espera que las conteste el alumnado en el
Examen Parcial: en prosa, nombrando la herramienta y explicando la consecuencia práctica. Sirven de
rúbrica.

## Licencia

Mismo régimen que `t3-ejercicios`: el código, bajo licencia [MIT](LICENSE), copyright © 2026
Alejandro Mesejo. El material docente del sitio web, bajo
[CC BY-NC-SA 4.0](https://creativecommons.org/licenses/by-nc-sa/4.0/deed.en).
