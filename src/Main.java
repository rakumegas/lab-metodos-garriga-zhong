/**
 * Zona de pruebas: ejecutenla para VER el resultado de sus metodos mientras avanzan.
 * Pueden modificar este archivo libremente (no se califica).
 *
 * Compilar:  javac -d out src/*.java
 * Ejecutar:  java -cp out Main
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("Potencia: " + Calculos.calcularPotencia(120, 5));
        Calculos.imprimirEncabezado("Ana Perez");
        System.out.println("Consumo 250 kWh: " + Calculos.clasificarConsumo(250));

        double[] lecturas = {10, 20, 30};
        System.out.println("Promedio: " + Calculos.promedio(lecturas));

        Medidor m = new Medidor("M-1", 100);
        m.registrarLectura(140);
        System.out.println("Consumo: " + m.consumoKwh() + " kWh, factura: $" + m.calcularFactura());
    }
}
