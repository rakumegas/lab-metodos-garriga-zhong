import javax.tools.*;
import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.*;

/**
 * AUTOGRADE - Laboratorio de Metodos en Java (Programacion 1)
 *
 * Se ejecuta solo en GitHub Actions en cada push:   java autograde/Autograde.java
 * (tambien pueden correrlo localmente para ver su nota; en local NO se sube nada).
 *
 * 
 */
public class Autograde {

    // =====================================================================
    // CONFIGURACION DEL DOCENTE
    // =====================================================================
    static final String SUPABASE_URL      = "https://ikusdplwwvcbxgvevkvw.supabase.co";       // ej: https://abcdxyz.supabase.co
    static final String SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImlrdXNkcGx3d3ZjYnhndmV2a3Z3Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODg5ODgyMjYsImV4cCI6MjEwNDU2NDIyNn0.TEA3ZOmZxv7zedc2hDZUaDoB0oGDVMXM2yh1M6D4YBc";  // anon / publishable key
    static final String TABLA             = "lab_metodos_entregas";

    static final int    MAX_AUTO   = 80;   // 70 codigo + 10 proceso (los otros 20 son manuales)
    static final int    TIMEOUT_S  = 4;    // segundos maximos por prueba

    // =====================================================================
    // ESTADO
    // =====================================================================
    static final PrintStream REAL_OUT = System.out;
    static final Path ROOT = Paths.get("").toAbsolutePath();
    static final LinkedHashMap<String, int[]> NIVELES = new LinkedHashMap<>();
    static final LinkedHashMap<String, String> TITULOS = new LinkedHashMap<>();
    static final List<String> FALLOS = new ArrayList<>();
    static final ExecutorService EX = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r); t.setDaemon(true); return t;
    });

    static Class<?> cCalc, cMed;
    static Ver V;
    static Random rnd;
    static final Class<?> D = double.class, I = int.class, DA = double[].class;
    static final Class<?>[] NONE = new Class<?>[0];

    interface Body { void run() throws Throwable; }

    static class Ver {
        double vmin, vmax, tarifa, bajo, medio;
        Ver(double a, double b, double c, double d, double e) { vmin = a; vmax = b; tarifa = c; bajo = d; medio = e; }
    }

    // =====================================================================
    // MAIN
    // =====================================================================
    public static void main(String[] args) throws Exception {
        TITULOS.put("N1", "Nivel 1 - Declaracion y retorno");
        TITULOS.put("N2", "Nivel 2 - Paso de parametros y arreglos");
        TITULOS.put("N3", "Nivel 3 - Sobrecarga");
        TITULOS.put("N4", "Nivel 4 - Clase Medidor (constructores y encapsulamiento)");
        TITULOS.put("N5", "Nivel 5 - Avanzado (varargs y recursion)");
        TITULOS.put("PROC", "Proceso y GitHub");
        for (String k : TITULOS.keySet()) NIVELES.put(k, new int[]{0, 0});

        REAL_OUT.println("==========================================================");
        REAL_OUT.println(" AUTOGRADE - Laboratorio: Metodos en Java");
        REAL_OUT.println("==========================================================");

        // ---------- equipo ----------
        Properties p = new Properties();
        Path eq = ROOT.resolve("equipo.properties");
        if (Files.exists(eq)) {
            try (Reader r = Files.newBufferedReader(eq, StandardCharsets.UTF_8)) { p.load(r); }
        }
        String[] n = {g(p, "integrante1.nombre"), g(p, "integrante2.nombre")};
        String[] a = {g(p, "integrante1.apellido"), g(p, "integrante2.apellido")};
        String[] c = {g(p, "integrante1.cedula"), g(p, "integrante2.cedula")};
        String verDecl = g(p, "version").toUpperCase();

        boolean equipoOk = true;
        for (int i = 0; i < 2; i++) {
            if (n[i].isEmpty() || a[i].isEmpty() || digits(c[i]).length() < 4) equipoOk = false;
        }
        if (equipoOk && digits(c[0]).equals(digits(c[1]))) equipoOk = false;

        String verCalc = "A";
        long seed = 12345L;
        if (equipoOk) {
            int suma = 0;
            for (char ch : (digits(c[0]) + digits(c[1])).toCharArray()) suma += ch - '0';
            verCalc = (suma % 2 == 0) ? "A" : "B";
            List<String> ds = new ArrayList<>(List.of(digits(c[0]), digits(c[1])));
            Collections.sort(ds);
            seed = String.join("", ds).hashCode();
        }
        V = verCalc.equals("A") ? new Ver(108, 132, 0.15, 100, 300) : new Ver(216, 264, 0.18, 150, 400);
        rnd = new Random(seed);

        REAL_OUT.println("Equipo: " + (equipoOk ? n[0] + " " + a[0] + " + " + n[1] + " " + a[1] : "(INCOMPLETO - revisen equipo.properties)"));
        REAL_OUT.println("Version que les corresponde: " + verCalc + "  (declarada: " + (verDecl.isEmpty() ? "-" : verDecl) + ")");
        REAL_OUT.println();

        // ---------- compilar ----------
        Path out = Files.createTempDirectory("lab-out");
        String errCompilacion = compilar(out);
        boolean compila = errCompilacion == null;

        if (compila) {
            try (URLClassLoader cl = new URLClassLoader(new URL[]{out.toUri().toURL()}, Autograde.class.getClassLoader())) {
                cCalc = cl.loadClass("Calculos");
                cMed = cl.loadClass("Medidor");
                pruebas();
            }
        } else {
            REAL_OUT.println("[X] NO COMPILA. Corrijan estos errores y vuelvan a hacer push:");
            REAL_OUT.println(errCompilacion);
            // registrar los puntos posibles como 0
            declararSinCompilar();
        }

        // ---------- proceso / git ----------
        String obs = proceso(compila, equipoOk, verDecl, verCalc);

        // ---------- huella anti-copia ----------
        List<Long> huella = huella();

        // ---------- reporte ----------
        int total = 0, posible = 0;
        REAL_OUT.println();
        REAL_OUT.println("-------------------- RESUMEN --------------------");
        StringBuilder md = new StringBuilder("## Resultado del autograde\n\n| Nivel | Puntos |\n|---|---|\n");
        for (Map.Entry<String, int[]> e : NIVELES.entrySet()) {
            REAL_OUT.printf("%-58s %2d / %2d%n", TITULOS.get(e.getKey()), e.getValue()[0], e.getValue()[1]);
            md.append("| ").append(TITULOS.get(e.getKey())).append(" | ").append(e.getValue()[0]).append(" / ").append(e.getValue()[1]).append(" |\n");
            total += e.getValue()[0]; posible += e.getValue()[1];
        }
        REAL_OUT.println("-------------------------------------------------");
        REAL_OUT.printf("NOTA AUTOMATICA: %d / %d%n", total, posible);
        REAL_OUT.println("Los 20 puntos restantes (REFLEXION.md = 10, defensa oral = 10) los evalua el profesor.");
        REAL_OUT.println("Observaciones automaticas: " + obs);
        md.append("\n**Nota automatica: ").append(total).append(" / ").append(posible).append("**\n\n");
        if (!FALLOS.isEmpty()) {
            md.append("### Pruebas que fallaron\n");
            for (String f : FALLOS) md.append("- ").append(f).append("\n");
        }
        String stepSummary = System.getenv("GITHUB_STEP_SUMMARY");
        if (stepSummary != null && !stepSummary.isEmpty()) {
            try { Files.writeString(Path.of(stepSummary), md.toString(), StandardOpenOption.APPEND, StandardOpenOption.CREATE); }
            catch (IOException ignored) { }
        }

        // ---------- subir ----------
        boolean fallo = false;
        boolean enCI = "true".equals(System.getenv("GITHUB_ACTIONS")) || "1".equals(System.getenv("LAB_FORCE_UPLOAD"));
        String url = envOr("LAB_SUPABASE_URL", SUPABASE_URL);
        String key = envOr("LAB_SUPABASE_KEY", SUPABASE_ANON_KEY);
        REAL_OUT.println();
        if (!enCI) {
            REAL_OUT.println("(Ejecucion local: no se sube la nota. La nota oficial se genera en GitHub Actions.)");
        } else if (url.startsWith("PEGAR") || key.startsWith("PEGAR")) {
            REAL_OUT.println("[!] Supabase no esta configurado en este template (avisen al profesor). No se subio la nota.");
        } else {
            fallo = !subir(url, key, n, a, c, equipoOk, verCalc, total, obs, huella);
        }
        System.exit(fallo ? 1 : 0);
    }

    // =====================================================================
    // COMPILACION
    // =====================================================================
    static String compilar(Path out) throws IOException {
        JavaCompiler jc = ToolProvider.getSystemJavaCompiler();
        if (jc == null) return "No se encontro el compilador (se necesita un JDK, no solo JRE).";
        List<File> files = new ArrayList<>();
        try (var s = Files.list(ROOT.resolve("src"))) {
            s.filter(f -> f.toString().endsWith(".java") && !f.getFileName().toString().equals("Main.java"))
             .forEach(f -> files.add(f.toFile()));
        }
        DiagnosticCollector<JavaFileObject> dc = new DiagnosticCollector<>();
        try (StandardJavaFileManager fm = jc.getStandardFileManager(dc, null, StandardCharsets.UTF_8)) {
            boolean ok = jc.getTask(null, fm, dc,
                    List.of("-d", out.toString(), "-encoding", "UTF-8"),
                    null, fm.getJavaFileObjectsFromFiles(files)).call();
            if (ok) return null;
        }
        StringBuilder sb = new StringBuilder();
        int k = 0;
        for (Diagnostic<? extends JavaFileObject> d : dc.getDiagnostics()) {
            if (d.getKind() != Diagnostic.Kind.ERROR) continue;
            String f = d.getSource() == null ? "?" : Path.of(d.getSource().getName()).getFileName().toString();
            sb.append("   ").append(f).append(":").append(d.getLineNumber()).append("  ").append(d.getMessage(Locale.ROOT).split("\n")[0]).append("\n");
            if (++k >= 8) { sb.append("   ...\n"); break; }
        }
        return sb.toString();
    }

    static void declararSinCompilar() {
        int[] pts = {12, 12, 12, 24, 10};
        String[] ks = {"N1", "N2", "N3", "N4", "N5"};
        for (int i = 0; i < ks.length; i++) NIVELES.get(ks[i])[1] += pts[i];
        FALLOS.add("El proyecto no compila: no se pudo ejecutar ninguna prueba");
    }

    // =====================================================================
    // PRUEBAS DE CODIGO
    // =====================================================================
    static void pruebas() throws Exception {
        REAL_OUT.println("--- " + TITULOS.get("N1"));
        test("N1", "calcularPotencia", 3, () -> {
            for (int i = 0; i < 4; i++) {
                double v = r(1, 300), c = r(0, 50);
                double got = (Double) callS("calcularPotencia", new Class<?>[]{D, D}, v, c);
                need(eq(got, v * c), "calcularPotencia(" + v + ", " + c + ") debia dar " + v * c + " y dio " + got);
            }
        });
        test("N1", "esVoltajeSeguro", 3, () -> {
            double[][] casos = {{V.vmin, 1}, {V.vmax, 1}, {(V.vmin + V.vmax) / 2, 1}, {V.vmin - 0.01, 0}, {V.vmax + 0.01, 0}, {0, 0}};
            for (double[] cs : casos) {
                boolean got = (Boolean) callS("esVoltajeSeguro", new Class<?>[]{D}, cs[0]);
                need(got == (cs[1] == 1), "esVoltajeSeguro(" + cs[0] + ") debia dar " + (cs[1] == 1));
            }
        });
        test("N1", "imprimirEncabezado", 3, () -> {
            String cli = List.of("Ana Perez", "Luis Mora", "Rosa Diaz").get(rnd.nextInt(3));
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            System.setOut(new PrintStream(bo, true, "UTF-8"));
            try { callS("imprimirEncabezado", new Class<?>[]{String.class}, cli); }
            finally { System.setOut(REAL_OUT); }
            String[] ls = bo.toString("UTF-8").trim().split("\\R");
            need(ls.length == 2 && !ls[0].isEmpty(), "debe imprimir exactamente 2 lineas");
            need(ls[0].trim().equals("=== FACTURA DE ENERGIA ==="), "linea 1 incorrecta: '" + ls[0] + "'");
            need(ls[1].trim().equals("Cliente: " + cli), "linea 2 debia ser 'Cliente: " + cli + "' y fue '" + ls[1] + "'");
        });
        test("N1", "clasificarConsumo", 3, () -> {
            Object[][] cs = {{V.bajo - 0.01, "BAJO"}, {V.bajo, "MEDIO"}, {V.medio - 0.01, "MEDIO"}, {V.medio, "ALTO"}, {0.0, "BAJO"}, {V.medio + 500, "ALTO"}};
            for (Object[] x : cs) {
                Object got = callS("clasificarConsumo", new Class<?>[]{D}, x[0]);
                need(x[1].equals(got), "clasificarConsumo(" + x[0] + ") debia dar " + x[1] + " y dio " + got);
            }
        });

        REAL_OUT.println("--- " + TITULOS.get("N2"));
        test("N2", "promedio", 3, () -> {
            for (int i = 0; i < 3; i++) {
                double[] arr = arr(2 + rnd.nextInt(5));
                double s = 0; for (double x : arr) s += x;
                double got = (Double) callS("promedio", new Class<?>[]{DA}, (Object) arr);
                need(eq(got, s / arr.length), "promedio(" + Arrays.toString(arr) + ") debia dar " + s / arr.length + " y dio " + got);
            }
            need(eq((Double) callS("promedio", new Class<?>[]{DA}, (Object) new double[0]), 0), "promedio de arreglo vacio debe ser 0");
        });
        test("N2", "aplicarFactor (modifica el original)", 3, () -> {
            double[] a = arr(4); double f = r(1, 5);
            double[] esp = a.clone(); for (int i = 0; i < esp.length; i++) esp[i] *= f;
            callS("aplicarFactor", new Class<?>[]{DA, D}, a, f);
            need(same(a, esp), "el arreglo original debia quedar " + Arrays.toString(esp) + " y quedo " + Arrays.toString(a));
        });
        test("N2", "copiaEscalada (no modifica el original)", 3, () -> {
            double[] a = arr(4); double f = r(1, 5); double[] orig = a.clone();
            double[] esp = a.clone(); for (int i = 0; i < esp.length; i++) esp[i] *= f;
            double[] res = (double[]) callS("copiaEscalada", new Class<?>[]{DA, D}, a, f);
            need(res != null, "devolvio null");
            need(res != a, "debe devolver un arreglo NUEVO, no el mismo objeto");
            need(same(a, orig), "el arreglo original NO debe cambiar");
            need(same(res, esp), "resultado esperado " + Arrays.toString(esp) + " y fue " + Arrays.toString(res));
        });
        test("N2", "contarSobreUmbral", 3, () -> {
            double[] a = {5, 10, 15, 10, 20, 7};
            need((Integer) callS("contarSobreUmbral", new Class<?>[]{DA, D}, a, 10.0) == 2, "con umbral 10 sobre {5,10,15,10,20,7} debe dar 2 (estrictamente mayores)");
            double[] b = arr(6); double u = b[2]; int esp = 0; for (double x : b) if (x > u) esp++;
            int got = (Integer) callS("contarSobreUmbral", new Class<?>[]{DA, D}, b, u);
            need(got == esp, "contarSobreUmbral(" + Arrays.toString(b) + ", " + u + ") debia dar " + esp + " y dio " + got);
        });

        REAL_OUT.println("--- " + TITULOS.get("N3"));
        test("N3", "calcularCosto(double)", 4, () -> {
            double k = r(10, 500);
            double got = (Double) callS("calcularCosto", new Class<?>[]{D}, k);
            need(eq(got, k * V.tarifa), "calcularCosto(" + k + ") debia dar " + k * V.tarifa + " (tarifa base " + V.tarifa + ") y dio " + got);
        });
        test("N3", "calcularCosto(double, double)", 4, () -> {
            double k = r(10, 500), t = r(0.05, 0.4);
            double got = (Double) callS("calcularCosto", new Class<?>[]{D, D}, k, t);
            need(eq(got, k * t), "calcularCosto(" + k + ", " + t + ") debia dar " + k * t + " y dio " + got);
        });
        test("N3", "calcularCosto(int, double, double)", 4, () -> {
            int d = 1 + rnd.nextInt(30); double kd = r(1, 20), t = r(0.05, 0.4);
            double got = (Double) callS("calcularCosto", new Class<?>[]{I, D, D}, d, kd, t);
            need(eq(got, d * kd * t), "calcularCosto(" + d + ", " + kd + ", " + t + ") debia dar " + d * kd * t + " y dio " + got);
        });

        REAL_OUT.println("--- " + TITULOS.get("N4"));
        Class<?>[] SD = {String.class, D};
        String srcMed = quitarComentarios(leer(ROOT.resolve("src/Medidor.java")));
        test("N4", "Medidor(String, double) guarda id y lectura", 3, () -> {
            double x = r(10, 500);
            Object m = nuevo(SD, "M-" + rnd.nextInt(90), x);
            need(callI(m, "getId", NONE) != null, "getId() devolvio null");
            need(eq((Double) callI(m, "getLecturaActual", NONE), x), "getLecturaActual() debia dar " + x);
        });
        test("N4", "Medidor(String, double) rechaza lectura negativa", 2, () -> {
            try { nuevo(SD, "X", -5.0); }
            catch (IllegalArgumentException e) { return; }
            catch (Throwable t) { throw new AssertionError("lanzo " + t + " en vez de IllegalArgumentException"); }
            throw new AssertionError("no lanzo IllegalArgumentException con lectura inicial negativa");
        });
        test("N4", "Medidor(String) inicia en 0", 2, () -> {
            Object m = nuevo(new Class<?>[]{String.class}, "Z-1");
            need("Z-1".equals(callI(m, "getId", NONE)), "el constructor de un argumento debe guardar el id");
            need(eq((Double) callI(m, "getLecturaActual", NONE), 0), "con un solo argumento la lectura debe ser 0");
            need(eq((Double) callI(m, "consumoKwh", NONE), 0), "el consumo inicial debe ser 0");
        });
        test("N4", "delegacion con this(...)", 1, () ->
            need(Pattern.compile("\\bthis\\s*\\(").matcher(srcMed).find(), "el constructor Medidor(String) debe usar this(...)"));
        test("N4", "getters (getId / getLecturaActual)", 2, () -> {
            Object m = nuevo(SD, "K-1", 100.0);
            callI(m, "registrarLectura", new Class<?>[]{D}, 150.0);
            need("K-1".equals(callI(m, "getId", NONE)), "getId() debia dar K-1");
            need(eq((Double) callI(m, "getLecturaActual", NONE), 150), "getLecturaActual() debia dar 150 tras registrar 150");
        });
        test("N4", "atributos private", 1, () -> {
            int cuenta = 0;
            for (Field f : cMed.getDeclaredFields()) {
                if (Modifier.isStatic(f.getModifiers())) continue;
                cuenta++;
                need(Modifier.isPrivate(f.getModifiers()), "el atributo '" + f.getName() + "' debe ser private");
            }
            need(cuenta >= 2, "declaren los atributos de la clase (id y lecturas)");
        });
        test("N4", "registrarLectura acepta lecturas validas", 3, () -> {
            Object m = nuevo(SD, "A-1", 200.0);
            need(Boolean.TRUE.equals(callI(m, "registrarLectura", new Class<?>[]{D}, 230.0)), "registrarLectura(230) sobre 200 debia devolver true");
            need(Boolean.TRUE.equals(callI(m, "registrarLectura", new Class<?>[]{D}, 230.0)), "una lectura IGUAL a la actual tambien es valida (solo se rechazan menores)");
        });
        test("N4", "registrarLectura rechaza lecturas menores", 3, () -> {
            Object m = nuevo(SD, "B-1", 200.0);
            need(Boolean.FALSE.equals(callI(m, "registrarLectura", new Class<?>[]{D}, 199.99)), "registrarLectura(199.99) sobre 200 debia devolver false");
            need(eq((Double) callI(m, "getLecturaActual", NONE), 200), "una lectura rechazada NO debe cambiar el estado");
            need(eq((Double) callI(m, "consumoKwh", NONE), 0), "una lectura rechazada NO debe cambiar el consumo");
        });
        test("N4", "consumoKwh", 3, () -> {
            Object m = nuevo(SD, "C-1", 100.0);
            callI(m, "registrarLectura", new Class<?>[]{D}, 130.0);
            need(eq((Double) callI(m, "consumoKwh", NONE), 30), "tras 100 -> 130 el consumo debe ser 30");
            callI(m, "registrarLectura", new Class<?>[]{D}, 170.0);
            need(eq((Double) callI(m, "consumoKwh", NONE), 40), "tras 130 -> 170 el consumo debe ser 40");
        });
        test("N4", "calcularFactura (usa Calculos.calcularCosto)", 4, () -> {
            double base = r(20, 100), k = r(10, 300);
            Object m = nuevo(SD, "D-1", base);
            callI(m, "registrarLectura", new Class<?>[]{D}, base + k);
            double got = (Double) callI(m, "calcularFactura", NONE);
            need(eq(got, k * V.tarifa), "consumo " + k + " kWh a tarifa base " + V.tarifa + " debia dar " + k * V.tarifa + " y dio " + got);
        });

        REAL_OUT.println("--- " + TITULOS.get("N5"));
        test("N5", "resistenciaSerie (varargs)", 3, () -> {
            double[] rs = {r(1, 100), r(1, 100), r(1, 100)};
            double s = rs[0] + rs[1] + rs[2];
            need(eq((Double) callS("resistenciaSerie", new Class<?>[]{DA}, (Object) rs), s), "serie de " + Arrays.toString(rs) + " debia dar " + s);
            need(eq((Double) callS("resistenciaSerie", new Class<?>[]{DA}, (Object) new double[0]), 0), "sin argumentos debe dar 0");
        });
        test("N5", "resistenciaParalelo (varargs)", 3, () -> {
            need(eq((Double) callS("resistenciaParalelo", new Class<?>[]{DA}, (Object) new double[]{100, 100}), 50), "100 || 100 debia dar 50");
            double[] rs = {r(1, 100), r(1, 100), r(1, 100)};
            double inv = 0; for (double x : rs) inv += 1 / x;
            double got = (Double) callS("resistenciaParalelo", new Class<?>[]{DA}, (Object) rs);
            need(eq(got, 1 / inv), "paralelo de " + Arrays.toString(rs) + " debia dar " + 1 / inv + " y dio " + got);
            need(eq((Double) callS("resistenciaParalelo", new Class<?>[]{DA}, (Object) new double[0]), 0), "sin argumentos debe dar 0");
        });
        test("N5", "sumaRecursiva (resultado)", 2, () -> {
            double[] a = arr(6); double s3 = a[0] + a[1] + a[2], sT = 0; for (double x : a) sT += x;
            need(eq((Double) callS("sumaRecursiva", new Class<?>[]{DA, I}, a, 0), 0), "n = 0 debe dar 0");
            need(eq((Double) callS("sumaRecursiva", new Class<?>[]{DA, I}, a, 3), s3), "los primeros 3 debian sumar " + s3);
            need(eq((Double) callS("sumaRecursiva", new Class<?>[]{DA, I}, a, a.length), sT), "todos debian sumar " + sT);
        });
        test("N5", "sumaRecursiva (es recursivo, sin for/while)", 2, () -> {
            String cuerpo = cuerpoMetodo(quitarComentarios(leer(ROOT.resolve("src/Calculos.java"))), "sumaRecursiva");
            need(cuerpo != null, "no se encontro el metodo sumaRecursiva");
            need(Pattern.compile("\\bsumaRecursiva\\s*\\(").matcher(cuerpo).find(), "el metodo debe llamarse a si mismo");
            need(!Pattern.compile("\\b(for|while)\\b").matcher(cuerpo).find(), "prohibido usar for o while en sumaRecursiva");
        });
    }

    // =====================================================================
    // PROCESO Y GIT  (10 pts)
    // =====================================================================
    static String proceso(boolean compila, boolean equipoOk, String verDecl, String verCalc) {
        REAL_OUT.println("--- " + TITULOS.get("PROC"));
        List<String> obs = new ArrayList<>();
        int codigoGanado = 0;
        for (String k : List.of("N1", "N2", "N3", "N4", "N5")) codigoGanado += NIVELES.get(k)[0];
        boolean compilaYAvanza = compila && codigoGanado > 0;
        score("PROC", "el proyecto compila y ya funciona algun metodo", compilaYAvanza ? 2 : 0, 2,
              compilaYAvanza ? null : (compila ? "todavia no hay ningun metodo implementado" : "corrijan los errores de compilacion"));
        score("PROC", "equipo.properties completo", equipoOk ? 2 : 0, 2, equipoOk ? null : "faltan nombres, apellidos o cedulas (o son iguales)");
        if (!equipoOk) obs.add("EQUIPO_INCOMPLETO");
        boolean vOk = equipoOk && verDecl.equals(verCalc);
        score("PROC", "version declarada correcta", vOk ? 1 : 0, 1, vOk ? null : "les corresponde la version " + verCalc);
        if (equipoOk && !vOk) obs.add("VERSION_DECLARADA_INCORRECTA(declaro " + (verDecl.isEmpty() ? "nada" : verDecl) + ", correspondia " + verCalc + ")");

        List<String[]> commits = commitsEstudiantes();
        int nc = commits.size();
        int ptsCommits = nc >= 4 ? 2 : (nc >= 2 ? 1 : 0);
        score("PROC", "al menos 4 commits de trabajo (" + nc + " encontrados)", ptsCommits, 2, ptsCommits == 2 ? null : "hagan commits frecuentes, uno por nivel");

        Set<String> autores = new HashSet<>();
        boolean coautor = false;
        long tMin = Long.MAX_VALUE, tMax = 0;
        for (String[] cm : commits) {
            autores.add(cm[1].toLowerCase());
            if (Pattern.compile("(?im)^co-authored-by:").matcher(cm[4]).find()) coautor = true;
            long t = Long.parseLong(cm[3].trim());
            tMin = Math.min(tMin, t); tMax = Math.max(tMax, t);
        }
        boolean dosAutores = autores.size() >= 2 || coautor;
        score("PROC", "ambos integrantes aparecen en el historial", dosAutores ? 2 : 0, 2, dosAutores ? null : "cada integrante debe hacer commits desde su usuario Git (o usar Co-authored-by)");
        long span = nc == 0 ? 0 : tMax - tMin;
        score("PROC", "trabajo repartido en el tiempo (>= 15 min entre primer y ultimo commit)", span >= 900 ? 1 : 0, 1, span >= 900 ? null : "todo el trabajo se subio en menos de 15 min");

        if (nc == 0) obs.add("SIN_COMMITS_DE_TRABAJO");
        else if (nc <= 2) obs.add("ENTREGA_EN_BLOQUE(" + nc + " commits)");
        else if (span < 300) obs.add("COMMITS_EN_RAFAGA(todo en " + span + " s)");
        if (nc > 0 && !dosAutores) obs.add("UN_SOLO_AUTOR_EN_GIT");
        return obs.isEmpty() ? "Sin senales automaticas" : String.join("; ", obs);
    }

    /** commits sin el commit raiz (el "Initial commit" del template). Campos: hash, email, nombre, epoch, cuerpo */
    static List<String[]> commitsEstudiantes() {
        List<String[]> out = new ArrayList<>();
        String raiz = git("rev-list", "--max-parents=0", "HEAD");
        String[] rs = raiz.split("\\R");
        String root = rs.length == 0 ? "" : rs[rs.length - 1].trim();
        String log = git("log", "--format=%H%x1f%ae%x1f%an%x1f%at%x1f%B%x1e");
        for (String rec : log.split("\u001e")) {
            rec = rec.replaceFirst("^\\s+", "");
            if (rec.isEmpty()) continue;
            String[] f = rec.split("\u001f", 5);
            if (f.length < 5 || f[0].equals(root)) continue;
            out.add(f);
        }
        return out;
    }

    // =====================================================================
    // HUELLA ANTI-COPIA (bottom-400 sketch de shingles de 5 tokens normalizados, sin el esqueleto original)
    // =====================================================================
    static List<Long> huella() {
        try {
            String raiz = git("rev-list", "--max-parents=0", "HEAD");
            String[] rs = raiz.split("\\R");
            String root = rs.length == 0 ? "" : rs[rs.length - 1].trim();
            Set<Long> base = new HashSet<>(), mio = new TreeSet<>();
            for (String f : List.of("src/Calculos.java", "src/Medidor.java")) {
                mio.addAll(shingles(leer(ROOT.resolve(f))));
                if (!root.isEmpty()) base.addAll(shingles(git("show", root + ":" + f)));
            }
            mio.removeAll(base);
            List<Long> l = new ArrayList<>(mio);
            Collections.sort(l);
            return l.size() > 400 ? new ArrayList<>(l.subList(0, 400)) : l;
        } catch (Exception e) { return new ArrayList<>(); }
    }

    static final Set<String> KEYWORDS = new HashSet<>(Arrays.asList(
        "public", "private", "protected", "static", "final", "void", "int", "double", "boolean", "long", "char", "String",
        "class", "new", "return", "if", "else", "for", "while", "do", "break", "continue", "this", "null", "true", "false",
        "throw", "throws", "try", "catch", "switch", "case", "default", "length", "Math"));

    /** Normaliza: identificadores -> I, numeros -> N, cadenas -> S (asi renombrar variables no engana la huella). */
    static Set<Long> shingles(String src) {
        String limpio = quitarComentarios(src).replaceAll("\"([^\"\\\\]|\\\\.)*\"", " \"S\" ");
        Matcher m = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*|\\d+\\.?\\d*|\\S").matcher(limpio);
        List<String> tk = new ArrayList<>();
        while (m.find()) {
            String t = m.group();
            if (Character.isLetter(t.charAt(0)) || t.charAt(0) == '_') tk.add(KEYWORDS.contains(t) ? t : "I");
            else if (Character.isDigit(t.charAt(0))) tk.add("N");
            else tk.add(t);
        }
        Set<Long> s = new HashSet<>();
        for (int i = 0; i + 5 <= tk.size(); i++) {
            long h = 0xcbf29ce484222325L;
            for (int j = i; j < i + 5; j++) {
                for (char ch : tk.get(j).toCharArray()) { h ^= ch; h *= 0x100000001b3L; }
                h ^= ' '; h *= 0x100000001b3L;
            }
            s.add(h & 0x7fffffffffffffffL);
        }
        return s;
    }

    // =====================================================================
    // SUBIDA A SUPABASE
    // =====================================================================
    static boolean subir(String url, String key, String[] n, String[] a, String[] c, boolean equipoOk,
                         String ver, int nota, String obs, List<Long> huella) throws Exception {
        String repo = envOr("GITHUB_REPOSITORY", ROOT.getFileName().toString());
        String hash = sha256(ROOT.resolve("autograde/Autograde.java"));
        Map<String, Object> niv = new LinkedHashMap<>();
        for (Map.Entry<String, int[]> e : NIVELES.entrySet()) niv.put(e.getKey(), Map.of("ganado", e.getValue()[0], "posible", e.getValue()[1]));
        Map<String, Object> detalle = new LinkedHashMap<>();
        detalle.put("niveles", niv);
        detalle.put("fallos", FALLOS);

        List<Object> filas = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            Map<String, Object> f = new LinkedHashMap<>();
            f.put("nombre", equipoOk ? n[i] : "(sin datos)");
            f.put("apellido", equipoOk ? a[i] : "(sin datos)");
            f.put("cedula", equipoOk ? c[i] : "SIN-DATOS-" + repo + "-" + (i + 1));
            f.put("companero_cedula", equipoOk ? c[1 - i] : null);
            f.put("grupo_repo", repo);
            f.put("version", ver);
            f.put("nota_auto", nota);
            f.put("observacion_plagio", obs);
            f.put("detalle", detalle);
            f.put("commit_sha", envOr("GITHUB_SHA", git("rev-parse", "HEAD")));
            f.put("run_id", envOr("GITHUB_RUN_ID", null));
            f.put("github_actor", envOr("GITHUB_ACTOR", null));
            f.put("autograde_sha256", hash);
            f.put("huella", huella);
            filas.add(f);
        }
        String body = json(filas);
        HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15)).build();
        for (int intento = 1; intento <= 3; intento++) {
            try {
                HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(url.replaceAll("/+$", "") + "/rest/v1/" + TABLA))
                        .timeout(Duration.ofSeconds(25))
                        .header("Content-Type", "application/json")
                        .header("apikey", key)
                        .header("Prefer", "return=minimal")
                        .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8));
                if (key.startsWith("eyJ")) b.header("Authorization", "Bearer " + key);
                HttpResponse<String> r = http.send(b.build(), HttpResponse.BodyHandlers.ofString());
                if (r.statusCode() / 100 == 2) {
                    REAL_OUT.println("[OK] Nota registrada en Supabase (" + repo + ").");
                    return true;
                }
                REAL_OUT.println("[!] Supabase respondio " + r.statusCode() + ": " + r.body());
                if (r.statusCode() / 100 == 4) return false;
            } catch (IOException e) {
                REAL_OUT.println("[!] Error de red (intento " + intento + "): " + e.getMessage());
            }
            Thread.sleep(2000L * intento);
        }
        return false;
    }

    // =====================================================================
    // UTILIDADES DE PRUEBA
    // =====================================================================
    static void test(String nivel, String nombre, int pts, Body b) {
        String motivo = null;
        Future<?> fut = EX.submit(() -> { try { b.run(); } catch (Throwable t) { throw new CompletionException(t); } });
        try {
            fut.get(TIMEOUT_S, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            fut.cancel(true);
            motivo = "tarda demasiado (posible ciclo infinito o recursion sin fin)";
        } catch (ExecutionException e) {
            Throwable t = e.getCause() instanceof CompletionException ? e.getCause().getCause() : e.getCause();
            if (t instanceof NoSuchMethodException) motivo = "no existe el metodo/constructor publico con esa firma: " + t.getMessage();
            else if (t instanceof AssertionError) motivo = t.getMessage();
            else if (t instanceof StackOverflowError) motivo = "StackOverflowError (recursion sin caso base)";
            else motivo = t.toString();
        } catch (InterruptedException e) {
            motivo = "interrumpido";
        }
        System.setOut(REAL_OUT);
        score(nivel, nombre, motivo == null ? pts : 0, pts, motivo);
    }

    static void score(String nivel, String nombre, int got, int max, String motivo) {
        int[] s = NIVELES.get(nivel);
        s[0] += got; s[1] += max;
        if (got == max) REAL_OUT.printf("  [OK] %-58s +%d%n", nombre, got);
        else {
            REAL_OUT.printf("  [X]  %-58s %d/%d%n", nombre, got, max);
            if (motivo != null) { REAL_OUT.println("         -> " + motivo); FALLOS.add(nombre + ": " + motivo); }
        }
    }

    static void need(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); }
    static boolean eq(double x, double y) { return Math.abs(x - y) < 1e-6; }
    static boolean same(double[] x, double[] y) {
        if (x == null || y == null || x.length != y.length) return false;
        for (int i = 0; i < x.length; i++) if (!eq(x[i], y[i])) return false;
        return true;
    }
    static double r(double lo, double hi) { return Math.round((lo + rnd.nextDouble() * (hi - lo)) * 100) / 100.0; }
    static double[] arr(int n) { double[] x = new double[n]; for (int i = 0; i < n; i++) x[i] = r(1, 100); return x; }

    static Object callS(String name, Class<?>[] t, Object... args) throws Throwable {
        Method m = cCalc.getMethod(name, t);
        need(Modifier.isStatic(m.getModifiers()), "'" + name + "' debe ser static");
        try { return m.invoke(null, args); } catch (InvocationTargetException e) { throw e.getCause(); }
    }
    static Object nuevo(Class<?>[] t, Object... args) throws Throwable {
        Constructor<?> c = cMed.getConstructor(t);
        try { return c.newInstance(args); } catch (InvocationTargetException e) { throw e.getCause(); }
    }
    static Object callI(Object o, String name, Class<?>[] t, Object... args) throws Throwable {
        Method m = cMed.getMethod(name, t);
        try { return m.invoke(o, args); } catch (InvocationTargetException e) { throw e.getCause(); }
    }

    // =====================================================================
    // UTILIDADES GENERALES
    // =====================================================================
    static String g(Properties p, String k) { return p.getProperty(k, "").trim(); }
    static String digits(String s) { return s.replaceAll("\\D", ""); }
    static String envOr(String k, String def) { String v = System.getenv(k); return (v == null || v.isEmpty()) ? def : v; }
    static String leer(Path p) { try { return Files.readString(p, StandardCharsets.UTF_8); } catch (IOException e) { return ""; } }
    static String quitarComentarios(String s) { return s.replaceAll("(?s)/\\*.*?\\*/", "").replaceAll("//[^\n]*", ""); }

    static String cuerpoMetodo(String src, String nombre) {
        Matcher m = Pattern.compile("\\b" + nombre + "\\s*\\([^)]*\\)\\s*\\{").matcher(src);
        if (!m.find()) return null;
        int i = m.end(), depth = 1;
        while (i < src.length() && depth > 0) {
            char ch = src.charAt(i++);
            if (ch == '{') depth++; else if (ch == '}') depth--;
        }
        return src.substring(m.end(), Math.max(m.end(), i - 1));
    }

    static String git(String... args) {
        try {
            List<String> cmd = new ArrayList<>(); cmd.add("git"); cmd.addAll(Arrays.asList(args));
            Process pr = new ProcessBuilder(cmd).directory(ROOT.toFile()).redirectErrorStream(false).start();
            String out = new String(pr.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            pr.waitFor();
            return pr.exitValue() == 0 ? out.trim() : "";
        } catch (Exception e) { return ""; }
    }

    static String sha256(Path p) {
        try {
            byte[] h = MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(p));
            StringBuilder sb = new StringBuilder(); for (byte b : h) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) { return null; }
    }

    static String json(Object o) {
        if (o == null) return "null";
        if (o instanceof String s) {
            StringBuilder sb = new StringBuilder("\"");
            for (char ch : s.toCharArray()) {
                switch (ch) {
                    case '"' -> sb.append("\\\"");
                    case '\\' -> sb.append("\\\\");
                    case '\n' -> sb.append("\\n");
                    case '\r' -> sb.append("\\r");
                    case '\t' -> sb.append("\\t");
                    default -> { if (ch < 0x20) sb.append(String.format("\\u%04x", (int) ch)); else sb.append(ch); }
                }
            }
            return sb.append('"').toString();
        }
        if (o instanceof Number || o instanceof Boolean) return o.toString();
        if (o instanceof Map<?, ?> m) {
            StringJoiner j = new StringJoiner(",", "{", "}");
            for (Map.Entry<?, ?> e : m.entrySet()) j.add(json(String.valueOf(e.getKey())) + ":" + json(e.getValue()));
            return j.toString();
        }
        if (o instanceof Collection<?> c) {
            StringJoiner j = new StringJoiner(",", "[", "]");
            for (Object x : c) j.add(json(x));
            return j.toString();
        }
        return json(o.toString());
    }
}
