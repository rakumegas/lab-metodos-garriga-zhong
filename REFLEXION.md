# Reflexión del equipo

**Instrucciones:** respondan **solo las 4 preguntas de su versión** (A o B), con **sus propias palabras** (3–5 líneas cada una)
y **citando nombres de métodos o líneas de SU código**. Las respuestas genéricas o iguales a las de otro grupo se califican en 0.
Escriban debajo de cada pregunta. (Se evalúa después; el autograde no califica este archivo.)

---

## VERSIÓN A

**A1.** `aplicarFactor` modifica el arreglo original, pero `copiaEscalada` no. Expliquen por qué, y qué es lo que
realmente se copia cuando le pasan un arreglo a un método.

> _Respuesta:_

**A2.** ¿Por qué Java no permite tener `double calcularCosto(double kwh)` y `int calcularCosto(double kwh)` en la misma clase?
¿Qué versión de `calcularCosto` elige Java para la llamada `calcularCosto(5, 2.5, 0.1)` y por qué?

> _Respuesta:_

**A3.** En `Medidor`, ¿para qué sirve `this(id, 0)` en el constructor de un solo parámetro? ¿Qué ventaja tiene frente a copiar y pegar el código del otro constructor?

> _Respuesta:_

**A4.** ¿Por qué los atributos de `Medidor` son `private`? ¿Qué protege `registrarLectura` y qué podría pasar si `lecturaActual` fuera público?

> _Respuesta:_

---

## VERSIÓN B

**B1.** Dibujen con texto (cajas y flechas) qué pasa en la memoria —variable `datos`, el arreglo y el parámetro del método—
cuando se ejecuta `aplicarFactor(datos, 2)`. ¿Por qué el arreglo original queda modificado?

> _Respuesta:_

**B2.** `imprimirEncabezado` es `void` y `clasificarConsumo` devuelve `String`. ¿Qué error da el compilador si olvidan un `return`
en alguna rama de `clasificarConsumo`? Expliquen con un caso de su código.

> _Respuesta:_

**B3.** ¿Por qué `sumaRecursiva` necesita un caso base? ¿Qué error aparece en Java si se omite y por qué ocurre?

> _Respuesta:_

**B4.** Si `Medidor` tuviera un atributo `double[] historial` y un getter que lo devolviera directamente, ¿qué riesgo hay para el
encapsulamiento? ¿Cómo se soluciona (idea de *copia defensiva*)?

> _Respuesta:_
