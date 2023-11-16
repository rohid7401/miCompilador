import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class faseSemantica {

    public static void analizar(Nodo ast) {
        // Recorrer el AST para verificar los identificadores
        verificarAST(ast);
    }

    private static void verificarAST(Nodo nodo) {
        if (nodo == null) return;    
        if (nodo instanceof ProgramaNodo) {
            ProgramaNodo programa = (ProgramaNodo) nodo;
            for (Nodo expresion : programa.expresiones) {
                verificarAST(expresion);
            }
        } else if (nodo instanceof IdentificadorNodo) {
            String nombre = ((IdentificadorNodo) nodo).nombre;
            if (!faseSintactica.tablaDeSimbolos.containsKey(nombre)) {
                escribirError("Error [Fase Semántica]: El identificador '" + nombre + "' no está definido.");
            }
        } else if (nodo instanceof AsignacionNodo) {
            verificarAST(((AsignacionNodo) nodo).izquierdo);
            verificarAST(((AsignacionNodo) nodo).derecho);
        } else if (nodo instanceof OperadorNodo) {
            verificarAST(((OperadorNodo) nodo).izquierdo);
            verificarAST(((OperadorNodo) nodo).derecho);
        }
    }
    

    private static void escribirError(String mensaje) {
        try (PrintWriter writer = new PrintWriter(new FileWriter("errores_semanticos.txt", true))) {
            writer.write(mensaje + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
