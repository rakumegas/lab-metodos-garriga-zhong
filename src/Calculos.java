/**
 * LABORATORIO: Metodos en Java - Medicion y facturacion de energia electrica.
 *
 * Completen cada metodo (quiten el "TODO" y escriban la logica).
 * NO cambien los nombres, tipos de parametros ni tipos de retorno: el autograde
 * los busca exactamente con esa firma.
 *
 * Los valores que dependen de su VERSION (A o B) estan en la tabla del README.
 */
public class Calculos {

    // ---------------------------------------------------------------
    // CONSTANTES DE SU VERSION (ver tabla en el README) - cambienlas
    // ---------------------------------------------------------------
    public static final double VOLTAJE_MIN = 216;   // TODO segun su version
    public static final double VOLTAJE_MAX = 264;   // TODO segun su version
    public static final double TARIFA_BASE = .18;   // TODO segun su version ($/kWh)
    public static final double LIMITE_BAJO = 150;   // TODO segun su version (kWh)
    public static final double LIMITE_MEDIO = 400;  // TODO segun su version (kWh)

    // ===============================================================
    // NIVEL 1 - Declaracion y retorno (basico)
    // ===============================================================

    /** Potencia en watts: P = V * I. */
    public static double calcularPotencia(double voltaje, double corriente) {
        double Potencia = voltaje * corriente;
        return Potencia;
    }

    /** true si VOLTAJE_MIN <= voltaje <= VOLTAJE_MAX (extremos incluidos). */
    public static boolean esVoltajeSeguro(double voltaje) {
        // TODO
        if (VOLTAJE_MIN <= voltaje && voltaje <= VOLTAJE_MAX) {
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Imprime EXACTAMENTE dos lineas:
     *   === FACTURA DE ENERGIA ===
     *   Cliente: <cliente>
     * (sin tildes). Este metodo no devuelve nada (void).
     */
    public static void imprimirEncabezado(String cliente) {
        System.out.println("=== FACTURA DE ENERGIA ===");
        System.out.println("Cliente: " + cliente);
    }

    /**
     * "BAJO" si kwh < LIMITE_BAJO; "MEDIO" si kwh < LIMITE_MEDIO; "ALTO" en otro caso.
     * Ojo: todas las rutas deben terminar en un return.
     */
    public static String clasificarConsumo(double kwh) {
        if (kwh < LIMITE_BAJO) {
            return "BAJO";
        } else if (kwh < LIMITE_MEDIO) {
            return "MEDIO";
        } else {
            return "ALTO";
        }
    }

    // ===============================================================
    // NIVEL 2 - Paso de parametros y arreglos (intermedio)
    // ===============================================================

    /** Promedio de las lecturas. Si el arreglo esta vacio devuelve 0. */
    public static double promedio(double[] lecturas) {
        if (lecturas.length == 0) {
            return 0;
        } else {
            double suma = 0;
            for (double lectura : lecturas) {
                suma += lectura;
            }
            return suma / lecturas.length;
        }
    }

    /** MODIFICA el arreglo recibido: multiplica cada lectura por factor. No devuelve nada. */
    public static void aplicarFactor(double[] lecturas, double factor) {
        for (int i = 0; i < lecturas.length; i++) {
            lecturas[i] *= factor;
        }
    }

    /** NO modifica el original: devuelve un arreglo NUEVO con cada lectura * factor. */
    public static double[] copiaEscalada(double[] lecturas, double factor) {
        double[] copia = new double[lecturas.length];
        for (int i = 0; i < lecturas.length; i++) {
            copia[i] = lecturas[i] * factor;
        }
        return copia;
    }

    /** Cuenta cuantas lecturas son ESTRICTAMENTE mayores que el umbral. */
    public static int contarSobreUmbral(double[] lecturas, double umbral) {
        int contador = 0;
        for (double lectura : lecturas) {
            if (lectura > umbral) {
                contador++;
            }
        }
        return contador;
    }

    // ===============================================================
    // NIVEL 3 - Sobrecarga (intermedio)
    // Mismo nombre "calcularCosto", distintos parametros.
    // ===============================================================

    /** kwh * TARIFA_BASE */
    public static double calcularCosto(double kwh) {
        return kwh * TARIFA_BASE;
    }

    /** kwh * tarifa */
    public static double calcularCosto(double kwh, double tarifa) {
        return kwh * tarifa;
    }

    /** dias * kwhPorDia * tarifa */
    public static double calcularCosto(int dias, double kwhPorDia, double tarifa) {
        return dias * kwhPorDia * tarifa;
    }

    // ===============================================================
    // NIVEL 5 - Avanzado (varargs y recursion)
    // (El Nivel 4 esta en Medidor.java)
    // ===============================================================

    /** Resistencia equivalente en SERIE: suma de todas. Sin argumentos devuelve 0. */
    public static double resistenciaSerie(double[] resistencias) {
        if (resistencias.length == 0) {
            return 0;
        } else {
            double suma = 0;
            for (double r : resistencias) {
                suma += r;
            }
            return suma;
        }
    }

    /** Resistencia equivalente en PARALELO: 1 / (1/R1 + 1/R2 + ...). Sin argumentos devuelve 0. */
    public static double resistenciaParalelo(double[] resistencias) {
        if (resistencias.length == 0) {
            return 0;
        }

        double sumaInversos = 0;
        for (double resistencia : resistencias) {
            if (resistencia == 0) {
                return 0;
            }
            sumaInversos += 1 / resistencia;
        }
        return 1 / sumaInversos;
    }

    /**
     * Suma de los primeros n elementos del arreglo, usando RECURSION.
     * Prohibido usar for o while dentro de este metodo.
     * Caso base: n <= 0 devuelve 0.
     */
    public static double sumaRecursiva(double[] datos, int n) {
        if (n <= 0) {
            return 0;
        }
        return datos[n - 1] + sumaRecursiva(datos, n - 1);
    }
}
