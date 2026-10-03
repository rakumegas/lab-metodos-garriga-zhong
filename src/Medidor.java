/**
 * NIVEL 4 - Clase Medidor: constructores, encapsulamiento y metodos de seguridad.
 *
 * Un medidor guarda su identificador y dos lecturas (en kWh):
 * la anterior y la actual. Los atributos DEBEN ser private.
 *
 * Declaren sus atributos aqui (minimo: id, lecturaAnterior, lecturaActual).
 */
public class Medidor {

    // TODO: declaren aqui los atributos (private)

    /**
     * Constructor principal. Guarda el id y deja lecturaAnterior = lecturaActual = lecturaInicial.
     * Si lecturaInicial < 0 debe lanzar: throw new IllegalArgumentException("Lectura invalida");
     */
    public Medidor(String id, double lecturaInicial) {
        // TODO
    }

    /**
     * Constructor de conveniencia: lectura inicial 0.
     * Debe DELEGAR en el otro constructor usando this(...).
     */
    public Medidor(String id) {
        // TODO
    }

    public String getId() {
        // TODO
        return null;
    }

    public double getLecturaActual() {
        // TODO
        return 0;
    }

    /**
     * "Setter" con validacion. Si nuevaLectura < lecturaActual (la lectura no puede
     * retroceder) NO cambia nada y devuelve false. Si es valida: lecturaAnterior toma el valor
     * de lecturaActual, lecturaActual toma nuevaLectura, y devuelve true.
     */
    public boolean registrarLectura(double nuevaLectura) {
        // TODO
        return false;
    }

    /** Consumo del ultimo periodo: lecturaActual - lecturaAnterior. */
    public double consumoKwh() {
        // TODO
        return 0;
    }

    /** Monto a pagar: usa Calculos.calcularCosto(...) con el consumo del ultimo periodo. */
    public double calcularFactura() {
        // TODO
        return 0;
    }
}
