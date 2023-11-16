//import java.io.*;

public class miCompilador {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Uso: java miCompilador [ARCHIVO_DE_ENTRADA] [ARCHIVO_DE_SALIDA]");
            return;
        }

        String archivoEntrada = args[0];
        //String archivoSalida = args[1];

        // Llamar a la fase léxica
        // faseLexica.analizar(archivoEntrada);

        // Llamar a la fase sintactica
        Nodo ast = faseSintactica.analizar(archivoEntrada);

        // LLamar al AST
        // AST.analizar(archivoEntrada);

        // Llamar a la fase sintactica
        faseSemantica.analizar(ast);

    }
}