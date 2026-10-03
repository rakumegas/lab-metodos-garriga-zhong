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
    public static final double VOLTAJE_MIN = 0;   // TODO segun su version
    public static final double VOLTAJE_MAX = 0;   // TODO segun su version
    public static final double TARIFA_BASE = 0;   // TODO segun su version ($/kWh)
    public static final double LIMITE_BAJO = 0;   // TODO segun su version (kWh)
    public static final double LIMITE_MEDIO = 0;  // TODO segun su version (kWh)

    // ===============================================================
    // NIVEL 1 - Declaracion y retorno (basico)
    // ===============================================================

    /** Potencia en watts: P = V * I. */
    public static double calcularPotencia(double voltaje, double corriente) {
        // TODO
        return 0;
    }

    /** true si VOLTAJE_MIN <= voltaje <= VOLTAJE_MAX (extremos incluidos). */
    public static boolean esVoltajeSeguro(double voltaje) {
        // TODO
        return false;
    }

    /**
     * Imprime EXACTAMENTE dos lineas:
     *   === FACTURA DE ENERGIA ===
     *   Cliente: <cliente>
     * (sin tildes). Este metodo no devuelve nada (void).
     */
    public static void imprimirEncabezado(String cliente) {
        // TODO
    }

    /**
     * "BAJO" si kwh < LIMITE_BAJO; "MEDIO" si kwh < LIMITE_MEDIO; "ALTO" en otro caso.
     * Ojo: todas las rutas deben terminar en un return.
     */
    public static String clasificarConsumo(double kwh) {
        // TODO
        return "";
    }

    // ===============================================================
    // NIVEL 2 - Paso de parametros y arreglos (intermedio)
    // ===============================================================

    /** Promedio de las lecturas. Si el arreglo esta vacio devuelve 0. */
    public static double promedio(double[] lecturas) {
        // TODO
        return 0;
    }

    /** MODIFICA el arreglo recibido: multiplica cada lectura por factor. No devuelve nada. */
    public static void aplicarFactor(double[] lecturas, double factor) {
        // TODO
    }

    /** NO modifica el original: devuelve un arreglo NUEVO con cada lectura * factor. */
    public static double[] copiaEscalada(double[] lecturas, double factor) {
        // TODO
        return null;
    }

    /** Cuenta cuantas lecturas son ESTRICTAMENTE mayores que el umbral. */
    public static int contarSobreUmbral(double[] lecturas, double umbral) {
        // TODO
        return 0;
    }

    // ===============================================================
    // NIVEL 3 - Sobrecarga (intermedio)
    // Mismo nombre "calcularCosto", distintos parametros.
    // ===============================================================

    /** kwh * TARIFA_BASE */
    public static double calcularCosto(double kwh) {
        // TODO
        return 0;
    }

    /** kwh * tarifa */
    public static double calcularCosto(double kwh, double tarifa) {
        // TODO
        return 0;
    }

    /** dias * kwhPorDia * tarifa */
    public static double calcularCosto(int dias, double kwhPorDia, double tarifa) {
        // TODO
        return 0;
    }

    // ===============================================================
    // NIVEL 5 - Avanzado (varargs y recursion)
    // (El Nivel 4 esta en Medidor.java)
    // ===============================================================

    /** Resistencia equivalente en SERIE: suma de todas. Sin argumentos devuelve 0. */
    public static double resistenciaSerie(double... resistencias) {
        // TODO
        return 0;
    }

    /** Resistencia equivalente en PARALELO: 1 / (1/R1 + 1/R2 + ...). Sin argumentos devuelve 0. */
    public static double resistenciaParalelo(double... resistencias) {
        // TODO
        return 0;
    }

    /**
     * Suma de los primeros n elementos del arreglo, usando RECURSION.
     * Prohibido usar for o while dentro de este metodo.
     * Caso base: n <= 0 devuelve 0.
     */
    public static double sumaRecursiva(double[] datos, int n) {
        // TODO
        return 0;
    }
}
