/**
 * NIVEL 4 - Clase Medidor: constructores, encapsulamiento y metodos de seguridad.
 *
 * Un medidor guarda su identificador y dos lecturas (en kWh):
 * la anterior y la actual. Los atributos DEBEN ser private.
 *
 * Declaren sus atributos aqui (minimo: id, lecturaAnterior, lecturaActual).
 */
public class Medidor {

    private String id;
    private double lecturaAnterior;
    private double lecturaActual;

    /**
     * Constructor principal. Guarda el id y deja lecturaAnterior = lecturaActual = lecturaInicial.
     * Si lecturaInicial < 0 debe lanzar: throw new IllegalArgumentException("Lectura invalida");
     */
    public Medidor(String id, double lecturaInicial) {
        if (lecturaInicial < 0) {
            throw new IllegalArgumentException("Lectura invalida");
        }
        this.id = id;
        this.lecturaAnterior = lecturaInicial;
        this.lecturaActual = lecturaInicial;
    }

    /**
     * Constructor de conveniencia: lectura inicial 0.
     * Debe DELEGAR en el otro constructor usando this(...).
     */
    public Medidor(String id) {
        this(id, 0);
    }

    public String getId() {
        return id;
    }

    public double getLecturaActual() {
        return lecturaActual;
    }

    /**
     * "Setter" con validacion. Si nuevaLectura < lecturaActual (la lectura no puede
     * retroceder) NO cambia nada y devuelve false. Si es valida: lecturaAnterior toma el valor
     * de lecturaActual, lecturaActual toma nuevaLectura, y devuelve true.
     */
    public boolean registrarLectura(double nuevaLectura) {
        if (nuevaLectura < lecturaActual) {
            return false;
        }

        lecturaAnterior = lecturaActual;
        lecturaActual = nuevaLectura;
        return true;
    }

    /** Consumo del ultimo periodo: lecturaActual - lecturaAnterior. */
    public double consumoKwh() {
        return lecturaActual - lecturaAnterior;
    }

    /** Monto a pagar: usa Calculos.calcularCosto(...) con el consumo del ultimo periodo. */
    public double calcularFactura() {
        return Calculos.calcularCosto(consumoKwh());
    }
}
