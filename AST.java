import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;


// Clase base para los nodos del árbol AST
class Nodo {
}

class ProgramaNodo extends Nodo {
    List<Nodo> expresiones = new ArrayList<>();

    void agregarExpresion(Nodo expresion) {
        expresiones.add(expresion);
    }
}

// Clase para los nodos que representan asignaciones
class AsignacionNodo extends Nodo {
    String asignador;
    Nodo izquierdo;
    Nodo derecho;

    AsignacionNodo(String asignador, Nodo izquierdo, Nodo derecho) {
        this.asignador = asignador;
        this.izquierdo = izquierdo;
        this.derecho = derecho;
    }
}

// Clase para los nodos que representan identificadores
class IdentificadorNodo extends Nodo {
    String nombre;

    IdentificadorNodo(String nombre) {
        this.nombre = nombre;
    }
}

// Clase para los nodos que representan números
class NumeroNodo extends Nodo {
    int valor;

    NumeroNodo(int valor) {
        this.valor = valor;
    }
}

// Clase para los nodos que representan operadores
class OperadorNodo extends Nodo {
    String operador;
    Nodo izquierdo;
    Nodo derecho;

    OperadorNodo(String operador, Nodo izquierdo, Nodo derecho) {
        this.operador = operador;
        this.izquierdo = izquierdo;
        this.derecho = derecho;
    }
}

public class AST {

    private static BufferedReader reader;
    private static String[] tokens;
    private static int tokenIndex = 0;
    private static int numeroDeLinea = 1;

    public static Nodo analizar(String archivoEntrada) {
        PrintWriter writer = null;  // Declarar la variable fuera del bloque try
        ProgramaNodo programa = new ProgramaNodo();
        try {
            reader = new BufferedReader(new FileReader(archivoEntrada));
            writer = new PrintWriter(new FileWriter("arbol.txt"));  // Inicializar la variable dentro del bloque try
            
            while ((tokens = obtenerTokens()) != null) {
                tokenIndex = 0;
                Nodo expresion = programa();
                programa.agregarExpresion(expresion);
                imprimirArbol(expresion, 0, writer);
                numeroDeLinea++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // Asegúrate de cerrar el PrintWriter en el bloque finally
            if (writer != null) {
                writer.close();
            }
            // Asegúrate de cerrar el BufferedReader en el bloque finally
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return programa;
    }

    private static void imprimirArbol(Nodo nodo, int indentacion, PrintWriter writer) {
        if (nodo == null) return;
    
        imprimirIndentacion(indentacion, writer);
        if (nodo instanceof IdentificadorNodo) {
            writer.println("Identificador: " + ((IdentificadorNodo) nodo).nombre);
        } else if (nodo instanceof NumeroNodo) {
            writer.println("Número: " + ((NumeroNodo) nodo).valor);
        } else if (nodo instanceof OperadorNodo) {
            writer.println("Operador: " + ((OperadorNodo) nodo).operador);
            imprimirArbol(((OperadorNodo) nodo).izquierdo, indentacion + 1, writer);
            imprimirArbol(((OperadorNodo) nodo).derecho, indentacion + 1, writer);
        } else if (nodo instanceof AsignacionNodo) {
            writer.println("Asignación: " + ((AsignacionNodo) nodo).asignador);
            imprimirArbol(((AsignacionNodo) nodo).izquierdo, indentacion + 1, writer);
            imprimirArbol(((AsignacionNodo) nodo).derecho, indentacion + 1, writer);
        }
    }
    
    private static void imprimirIndentacion(int indentacion, PrintWriter writer) {
        for (int i = 0; i < indentacion; i++) {
            if (i < indentacion - 1) {
                writer.print("|   ");
            } else {
                writer.print("|---");
            }
        }
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


    private static Nodo programa() {
        Nodo resultado = expresion();
        expect(";");
        return resultado;
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
                // String identificador = ((IdentificadorNodo) izquierdo).nombre;
                // Crear nodo de asignación
                izquierdo = new AsignacionNodo(operador, izquierdo, derecho);
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
        } else if (token.matches("[a-z]")) {
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
            writer.write("Error [Fase Sintáctica] en la línea " + numeroDeLinea + ": " + mensaje + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Lanzar una excepción específica para manejar errores sintácticos
        throw new RuntimeException("Error sintáctico: " + mensaje);
    }

    public static void main(String[] args) {
        // Cambiar la ruta del archivo de entrada según tu necesidad
        analizar("entrada.txt");
    }
}

