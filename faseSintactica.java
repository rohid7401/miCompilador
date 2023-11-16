import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashMap;

public class faseSintactica {

    private static BufferedReader reader;
    private static String[] tokens;
    private static int tokenIndex = 0;
    private static int numeroDeLinea = 1;
    public static HashMap<String, Nodo> tablaDeSimbolos = new HashMap<>();

    public static Nodo analizar(String archivoEntrada) {
        ProgramaNodo programa = new ProgramaNodo(); // Crear un nodo para el programa completo
        try {
            reader = new BufferedReader(new FileReader(archivoEntrada));
            while ((tokens = obtenerTokens()) != null) {
                tokenIndex = 0;
                Nodo expresion = programa();  // Construir el AST para cada expresión
                programa.agregarExpresion(expresion); // Agregar la expresión al programa
                numeroDeLinea++;
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return programa;  // Retornar el nodo del programa, que contiene todas las expresiones
    }
    

    private static Nodo programa() {
        Nodo resultado = expresion();
        expect(";");
        return resultado;
    }

    private static String[] obtenerTokens() throws IOException {
        String linea = reader.readLine();
        while (linea != null && (linea.trim().isEmpty() || linea.trim().startsWith("import"))) {
            linea = reader.readLine();
            numeroDeLinea++;
        }
        if (linea != null) {
            // Dividir la línea en tokens eliminando espacios alrededor de los operadores
            return linea.split("\\s*(?=[=;()+\\-*\\/])|(?<=[=;()+\\-*\\/])\\s*");
        }
        return null;
    }
    

    private static String obtenerTokenActual() {
        if (tokens != null && tokenIndex < tokens.length) {
            return tokens[tokenIndex];
        }
        return null;
    }

    private static void avanzar() {
        tokenIndex++;
    }

    private static void expect(String tokenEsperado) {
        String token = obtenerTokenActual();
        if (token == null || !token.equals(tokenEsperado)) {
            error("Token esperado '" + tokenEsperado + "' no encontrado.");
        }
        avanzar();
    }

    private static Nodo expresion() {
        Nodo izquierdo = termino();
        while (obtenerTokenActual() != null && (obtenerTokenActual().equals("+") || obtenerTokenActual().equals("-") || obtenerTokenActual().equals("="))) {
            String operador = obtenerTokenActual();
            avanzar();
            Nodo derecho = termino();
    
            if (operador.equals("=")) {
                if (!(izquierdo instanceof IdentificadorNodo)) {
                    error("Lado izquierdo de la asignacion debe ser un identificador.");
                }
                String identificador = ((IdentificadorNodo) izquierdo).nombre;
                // Actualizar tabla de símbolos
                tablaDeSimbolos.put(identificador, derecho);
                // Crear nodo de asignación
                izquierdo = new AsignacionNodo(identificador, izquierdo, derecho);
            } else {
                izquierdo = new OperadorNodo(operador, izquierdo, derecho);
            }
        }
        return izquierdo;
    }
    
    private static Nodo termino() {
        Nodo izquierdo = factor();
        while (obtenerTokenActual() != null && (obtenerTokenActual().equals("*") || obtenerTokenActual().equals("/"))) {
            String operador = obtenerTokenActual();
            avanzar();
            Nodo derecho = factor();
            izquierdo = new OperadorNodo(operador, izquierdo, derecho);
        }
        return izquierdo;
    }

    private static Nodo factor() {
        String token = obtenerTokenActual();
        if (token == null) {
            error("Fin inesperado de archivo.");
            return null;
        } else if (token.matches("[a-z]+")) {
            avanzar();
            return new IdentificadorNodo(token);
        } else if (token.matches("\\d+")) {
            avanzar();
            return new NumeroNodo(Integer.parseInt(token));
        } else if (token.equals("(")) {
            avanzar();
            Nodo expresion = expresion();
            expect(")");
            return expresion;
        } else {
            error("Token no esperado '" + token + "'.");
            return null;
        }
    }
    
    private static void error(String mensaje) {
        try (PrintWriter writer = new PrintWriter(new FileWriter("errores_sintacticos.txt", true))) {
            writer.write("Error [Fase Sintactica] en la linea " + numeroDeLinea + ": " + mensaje + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
