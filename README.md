# Laboratorio: Métodos en Java — Medición y facturación de energía

**Programación 1 · Unidad 2.5 · Trabajo en parejas · Duración: 60 minutos**

Van a construir el "cerebro" de un sistema simple de medidores eléctricos usando **métodos**: desde declarar
uno y devolver un valor, hasta sobrecarga, constructores, encapsulamiento y recursión.

> Ustedes ya vieron la teoría (declaración, paso de parámetros, retorno, constructores, sobrecarga y métodos de
> seguridad). Aquí la ponen en práctica. **Lean los comentarios (`/** ... */`) de cada método: ahí está lo que debe hacer.**

---

## 1. Antes de empezar (5 min)

1. **Un** integrante abre este repositorio y pulsa **Use this template → Create a new repository**.
   Nómbrenlo `lab-metodos-apellido1-apellido2` y márquenlo **Private**.
2. En su nuevo repo: **Settings → Collaborators** → agreguen a su compañero/a **y a `SarmientoEdu`**.
3. Cada quien clona el repo en su computadora (`git clone ...`) y configura su identidad de Git:
   ```
   git config user.name  "Nombre Apellido"
   git config user.email "su-correo@ejemplo.com"
   ```
4. Abran `equipo.properties` y complétenlo (nombres, apellidos, cédulas y **versión**). Hagan su **primer commit**.

### ¿Qué versión (A o B) me toca?
Sumen **todos los dígitos** de las dos cédulas del equipo (ignoren letras y guiones). **Par → A · Impar → B.**

| Constante (en `Calculos.java`) | Versión A | Versión B |
|---|---|---|
| `VOLTAJE_MIN` | 108 | 216 |
| `VOLTAJE_MAX` | 132 | 264 |
| `TARIFA_BASE` | 0.15 | 0.18 |
| `LIMITE_BAJO` | 100 | 150 |
| `LIMITE_MEDIO` | 300 | 400 |

Si eligen la versión equivocada, el autograde califica con la que **les corresponde** y sus pruebas fallarán.

---

## 2. Cómo trabajar en pareja

- **Un teclado, dos cerebros:** uno escribe (*piloto*) y el otro revisa y dicta (*copiloto*). **Cambien de rol en cada nivel.**
- **Cada integrante debe hacer commits** desde su propia cuenta/identidad Git (o usar `Co-authored-by:` en el mensaje).
- Hagan **un commit al terminar cada nivel** (mínimo 4 en total): `git add . && git commit -m "Nivel 2 listo" && git push`.

---

## 3. Plan sugerido (60 min)

| Min | Qué hacer |
|---|---|
| 0–5 | Repo, `equipo.properties`, primer commit |
| 5–13 | **Nivel 1** — Declaración y retorno |
| 13–21 | **Nivel 2** — Paso de parámetros y arreglos |
| 21–28 | **Nivel 3** — Sobrecarga |
| 28–43 | **Nivel 4** — Clase `Medidor` (constructores y encapsulamiento) |
| 43–51 | **Nivel 5** — Avanzado (varargs y recursión) |
| 51–56 | `REFLEXION.md` (respondan solo las preguntas de **su versión**, con sus palabras) |
| 56–60 | Último `push` y revisar el resultado en **Actions** |

Si el tiempo no alcanza, **prioricen los niveles 1–4**: valen 60 de los 100 puntos.

---

## 4. Los niveles

| Nivel | Archivo | Qué practican | Puntos |
|---|---|---|---|
| 1 | `src/Calculos.java` | Firma de un método, `return`, `void`, `if/else` con `return` en todas las rutas | 12 |
| 2 | `src/Calculos.java` | Arreglos como parámetro: modificar el original vs. devolver una copia | 12 |
| 3 | `src/Calculos.java` | Sobrecarga de `calcularCosto` (3 versiones) | 12 |
| 4 | `src/Medidor.java` | Atributos `private`, dos constructores (`this(...)`), validación, getters | 24 |
| 5 | `src/Calculos.java` | `varargs` (`double...`) y recursión | 10 |
| — | Git / equipo | Compila, equipo completo, commits de ambos, trabajo repartido | 10 |
| — | `REFLEXION.md` + defensa oral | Lo evalúa el profesor | 20 |

**Reglas de oro**
- No cambien nombres de métodos, tipos de parámetros ni tipos de retorno (el autograde los busca exactos).
- Los nombres de variables dentro de sus métodos son libres.
- `src/Main.java` es solo para que ustedes prueben; no se califica.

---

## 5. Probar en su computadora

```bash
javac -d out src/*.java      # compilar
java -cp out Main            # correr sus propias pruebas en Main.java
java autograde/Autograde.java  # ver su nota estimada (en local NO se sube nada)
```
Requiere **JDK 17 o superior**. También pueden usar el botón *Run* de VS Code / su IDE.

## 6. Entrega

Se entrega **haciendo `git push` a la rama `main`**. GitHub Actions corre el autograde solo:
pestaña **Actions → último workflow → Summary** para ver su puntaje y qué pruebas fallaron.
Pueden hacer push las veces que quieran durante la clase; **cuenta la última entrega dentro del horario del laboratorio**.

⚠️ **No modifiquen** la carpeta `autograde/` ni `.github/`. Además del código, el profesor revisa el historial de
commits, compara similitud entre grupos y hace una **defensa oral individual** (explicar y modificar en vivo un método suyo).

Rúbrica completa: [`RUBRICA.md`](RUBRICA.md)
