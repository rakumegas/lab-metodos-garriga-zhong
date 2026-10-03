# Rúbrica — Laboratorio: Métodos en Java (100 puntos)

**Nota final = Autograde (80) + Reflexión (10) + Defensa oral (10).**
El autograde publica su parte automáticamente; el profesor completa los 20 puntos restantes.

## A. Autograde — 80 puntos (automático)

| Nivel | Criterio | Pts |
|---|---|---|
| **1. Declaración y retorno (12)** | `calcularPotencia` devuelve V·I | 3 |
| | `esVoltajeSeguro` respeta el rango de su versión, extremos incluidos | 3 |
| | `imprimirEncabezado` (`void`) imprime exactamente las 2 líneas pedidas | 3 |
| | `clasificarConsumo` devuelve BAJO / MEDIO / ALTO con los límites de su versión | 3 |
| **2. Parámetros y arreglos (12)** | `promedio` (incluye arreglo vacío → 0) | 3 |
| | `aplicarFactor` modifica el arreglo original | 3 |
| | `copiaEscalada` devuelve un arreglo nuevo y no toca el original | 3 |
| | `contarSobreUmbral` cuenta estrictamente mayores | 3 |
| **3. Sobrecarga (12)** | `calcularCosto(double)` usa `TARIFA_BASE` | 4 |
| | `calcularCosto(double, double)` | 4 |
| | `calcularCosto(int, double, double)` | 4 |
| **4. Clase `Medidor` (24)** | `Medidor(String, double)` guarda id y lectura | 3 |
| | Rechaza lectura inicial negativa con `IllegalArgumentException` | 2 |
| | `Medidor(String)` inicia en 0 | 2 |
| | El constructor de un argumento delega con `this(...)` | 1 |
| | Getters `getId` / `getLecturaActual` | 2 |
| | Todos los atributos son `private` | 1 |
| | `registrarLectura` acepta lecturas válidas (incluye igual a la actual) | 3 |
| | `registrarLectura` rechaza lecturas menores sin cambiar el estado | 3 |
| | `consumoKwh` = actual − anterior | 3 |
| | `calcularFactura` usa `Calculos.calcularCosto` con la tarifa de su versión | 4 |
| **5. Avanzado (10)** | `resistenciaSerie` con `varargs` | 3 |
| | `resistenciaParalelo` con `varargs` | 3 |
| | `sumaRecursiva` correcta | 2 |
| | `sumaRecursiva` es realmente recursiva (se llama a sí misma, sin `for`/`while`) | 2 |
| **Proceso y GitHub (10)** | El proyecto compila | 2 |
| | `equipo.properties` completo y válido | 2 |
| | Versión declarada = versión que corresponde | 1 |
| | ≥ 4 commits de trabajo (2–3 commits = 1 pt) | 2 |
| | Ambos integrantes aparecen en el historial (autores distintos o `Co-authored-by`) | 2 |
| | Trabajo repartido en el tiempo (≥ 15 min entre primer y último commit) | 1 |

Las pruebas usan valores generados a partir de las cédulas del equipo: **no sirve escribir respuestas fijas**.

## B. Reflexión — 10 puntos (`REFLEXION.md`, lo califica el profesor)

Cuatro preguntas de la versión del equipo, **2.5 pts cada una**.

| Nivel de logro | Puntos por pregunta | Descripción |
|---|---|---|
| Excelente | 2.5 | Explicación correcta, con sus propias palabras y **citando su código** |
| Bueno | 1.5–2 | Correcta pero genérica, o con un detalle impreciso |
| Regular | 0.5–1 | Parcialmente correcta o copiada de la teoría sin conectarla con su código |
| Insuficiente | 0 | Vacía, incorrecta o idéntica a la de otro grupo |

## C. Defensa oral individual — 10 puntos (en clase, ~2 min por estudiante)

El profesor elige **un método al azar** del repositorio del equipo y pregunta a **cada integrante por separado**.

| Criterio | Excelente | Bueno | Regular | Insuficiente | Máx. |
|---|---|---|---|---|---|
| Explica qué hace el método, línea por línea | 4 | 3 | 1–2 | 0 | 4 |
| Modifica el método en vivo (p. ej. cambiar una regla o un límite) | 4 | 3 | 1–2 | 0 | 4 |
| Reconoce su aporte y el de su pareja en el trabajo | 2 | 1 | 0 | 0 | 2 |

## D. Observaciones automáticas (no restan puntos por sí solas)

El autograde deja una observación para revisión del profesor cuando detecta: entrega en bloque (≤ 2 commits),
commits en ráfaga (todo en < 5 min), un solo autor en Git, versión declarada incorrecta o equipo incompleto.
Además se compara la similitud del código entre grupos. **Son señales, no pruebas:** cualquier sospecha se
confirma con la defensa oral y puede llevar a la anulación del laboratorio según el reglamento de la asignatura.
