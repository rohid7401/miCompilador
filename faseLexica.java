import java.io.*;
import java.util.HashMap;

public class faseLexica {

    private static HashMap<String, String> tablaDeSimbolos = new HashMap<>();

    public static void analizar(String archivoEntrada) {

        try {

            BufferedReader reader = new BufferedReader(new FileReader(archivoEntrada));
            String linea;
            int numeroDeLinea = 1;

            FileWriter fileWriter = new FileWriter("errores_lexicos.txt");

            while ((linea = reader.readLine()) != null) {
            String resultado = tokenizarLinea(linea, numeroDeLinea);
            fileWriter.write(resultado);
            numeroDeLinea++;
        }
            reader.close();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String tokenizarLinea(String linea, int numeroDeLinea) {
        StringBuilder resultado = new StringBuilder();
        StringBuilder errores = new StringBuilder();

        String[] tokens = linea.split("\\s+|(?=[=;()+\\-*\\/])|(?<=[=;()+\\-*\\/])");

        for (String token : tokens) {
            token = token.trim();

            if (token.isEmpty()) {
                continue;
            }

            if (token.matches("\\d+")) {
                resultado.append("Token: " + token + ", Tipo: NUMERO\n");
            } else if (token.matches("[a-z]")) {
                resultado.append("Token: " + token + ", Tipo: IDENTIFICADOR\n");

                tablaDeSimbolos.put(token, null);
            } else if (token.equals("=")) {
                resultado.append("Token: " + token + ", Tipo: ASIGNACION\n");
                
                if (!tablaDeSimbolos.isEmpty()) {
                    String ultimoIdentificador = tablaDeSimbolos.keySet().iterator().next();
                    tablaDeSimbolos.put(ultimoIdentificador, null);
                }
            } else if (token.equals(";")) {
                resultado.append("Token: " + token + ", Tipo: PUNTO_COMA\n");
            } else if (token.equals("+")) {
                resultado.append("Token: " + token + ", Tipo: SUMA\n");
            } else if (token.equals("-")) {
                resultado.append("Token: " + token + ", Tipo: RESTA\n");
            } else if (token.equals("*")) {
                resultado.append("Token: " + token + ", Tipo: MULTIPLICACION\n");
            } else if (token.equals("/")) {
                resultado.append("Token: " + token + ", Tipo: DIVISION\n");
            } else if (token.equals("(")) {
                resultado.append("Token: " + token + ", Tipo: PARENTESIS_IZQ\n");
            } else if (token.equals(")")) {
                resultado.append("Token: " + token + ", Tipo: PARENTESIS_DER\n");
            } else {
                errores.append("Error [Fase Léxica]: La línea " + numeroDeLinea + " contiene un error, lexema no reconocido: " + token + "\n");
            }
        }

        return errores.toString();
    }
}